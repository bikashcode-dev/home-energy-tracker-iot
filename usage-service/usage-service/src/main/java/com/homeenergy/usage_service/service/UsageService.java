package com.homeenergy.usage_service.service;

import com.homeenergy.kafka.event.AlertingEvent;
import com.homeenergy.kafka.event.EneragyUsageEvent;
import com.homeenergy.usage_service.client.DeviceClient;
import com.homeenergy.usage_service.client.UserClient;
import com.homeenergy.usage_service.dto.DeviceDto;
import com.homeenergy.usage_service.dto.UserDto;
import com.homeenergy.usage_service.model.DeviceEnergy;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsageService {

    private static final String USAGE_TOPIC = "energy-usage-topic";
    private static final String ALERT_TOPIC = "energy-alerts";
    private static final String MEASUREMENT_NAME = "energy_usage";
    private static final String FIELD_ENERGY_CONSUMED = "energyConsumed";
    private static final String TAG_DEVICE_ID = "deviceId";


    private final InfluxDBClient influxDBClient;
    private final DeviceClient deviceClient;
    private final UserClient userClient;
    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    @Value("${energy.aggregation.window-seconds}")
    private long aggregationWindowSeconds;

    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;

    public UsageService(
            InfluxDBClient influxDBClient,
            UserClient userClient,
            DeviceClient deviceClient,
            KafkaTemplate<String, AlertingEvent> kafkaTemplate) {
        this.influxDBClient = influxDBClient;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = USAGE_TOPIC, groupId = "usage-service")
    public void energyUsageEvent(EneragyUsageEvent energyUsageEvent) {
        log.info("Received energy usage event: {}", energyUsageEvent);
        Point point = Point.measurement(MEASUREMENT_NAME)
                .addTag(TAG_DEVICE_ID, String.valueOf(energyUsageEvent.deviceId()))
                .addField(FIELD_ENERGY_CONSUMED, energyUsageEvent.energyConsumed())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);

        influxDBClient.getWriteApiBlocking().writePoint(influxBucket, influxOrg, point);
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() {
        final Instant now = Instant.now();
        final Instant windowStart = now.minusSeconds(aggregationWindowSeconds);

        List<FluxTable> tables = getFluxTables(windowStart, now);
        List<DeviceEnergy> deviceEnergies = extractDeviceEnergies(tables);

        if (deviceEnergies.isEmpty()) {
            log.debug("No energy usage records found between {} and {}", windowStart, now);
            return;
        }

        log.info("Aggregated energy usage events: {}", deviceEnergies);

        enrichWithUserIds(deviceEnergies);
        deviceEnergies.removeIf(deviceEnergy -> deviceEnergy.getUserId() == null);

        if (deviceEnergies.isEmpty()) {
            return;
        }

        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap = deviceEnergies.stream()
                .collect(Collectors.groupingBy(DeviceEnergy::getUserId));
        log.info("User-Device Energy Map: {}", userDeviceEnergyMap);

        evaluateThresholdsAndNotify(userDeviceEnergyMap);
    }

    private List<DeviceEnergy> extractDeviceEnergies(List<FluxTable> tables) {
        List<DeviceEnergy> deviceEnergies = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String deviceIdStr = (String) record.getValueByKey(TAG_DEVICE_ID);
                if (deviceIdStr == null) {
                    continue;
                }

                double energyConsumed = record.getValueByKey("_value") instanceof Number num
                        ? num.doubleValue()
                        : 0.0;

                deviceEnergies.add(DeviceEnergy.builder()
                        .deviceId(Long.valueOf(deviceIdStr))
                        .energyConsumed(energyConsumed)
                        .build());
            }
        }
        return deviceEnergies;
    }

    private void enrichWithUserIds(List<DeviceEnergy> deviceEnergies) {
        Map<Long, Long> deviceToUserCache = new HashMap<>();

        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            Long deviceId = deviceEnergy.getDeviceId();
            if (deviceToUserCache.containsKey(deviceId)) {
                deviceEnergy.setUserId(deviceToUserCache.get(deviceId));
                continue;
            }

            try {
                final DeviceDto deviceResponse = deviceClient.getDeviceById(deviceId);
                if (deviceResponse == null || deviceResponse.id() == null) {
                    log.error("Device with id {} not found", deviceId);
                    continue;
                }
                deviceToUserCache.put(deviceId, deviceResponse.userId());
                deviceEnergy.setUserId(deviceResponse.userId());
            } catch (Exception e) {
                log.warn("Failed to fetch device ID {}: {}", deviceId, e.getMessage());
            }
        }
    }

    private void evaluateThresholdsAndNotify(Map<Long, List<DeviceEnergy>> userDeviceEnergyMap) {
        for (Map.Entry<Long, List<DeviceEnergy>> entry : userDeviceEnergyMap.entrySet()) {
            Long userId = entry.getKey();
            try {
                UserDto user = userClient.getUserById(userId);
                if (user == null || user.id() == null || !user.alerting() || user.energyAlertingThreshold() <= 0) {
                    log.warn("User not found, alerting disabled, or invalid threshold for ID: {}", userId);
                    continue;
                }

                double totalConsumption = entry.getValue().stream()
                        .mapToDouble(DeviceEnergy::getEnergyConsumed)
                        .sum();

                processUserThreshold(user, totalConsumption);
            } catch (Exception e) {
                log.warn("Failed to fetch user ID {}: {}", userId, e.getMessage());
            }
        }
    }

    private void processUserThreshold(UserDto user, double totalConsumption) {
        double threshold = user.energyAlertingThreshold();
        Long userId = user.id();

        if (totalConsumption > threshold) {
            log.info("Alerting User ID {} has exceeded the energy threshold! Total Consumption: {}, Threshold: {}",
                    userId, totalConsumption, threshold);

            final AlertingEvent alertingEvent = AlertingEvent.builder()
                    .userId(userId)
                    .message("Energy Consumption threshold exceeded")
                    .threshold(threshold)
                    .emergyConsumed(totalConsumption)
                    .email(user.email())
                    .build();

            kafkaTemplate.send(ALERT_TOPIC, String.valueOf(userId), alertingEvent)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish alert event for User ID {}: {}", userId, ex.getMessage());
                        }
                    });
        } else {
            log.info("User ID {} is within the energy threshold. Total Consumption: {}, Threshold: {}",
                    userId, totalConsumption, threshold);
        }
    }

    private List<FluxTable> getFluxTables(Instant windowStart, Instant now) {
        String fluxQuery = String.format("""
                from(bucket: "%s")
                  |> range(start: time(v: "%s"), stop: time(v: "%s"))
                  |> filter(fn: (r) => r["_measurement"] == "%s")
                  |> filter(fn: (r) => r["_field"] == "%s")
                  |> group(columns: ["%s"])
                  |> sum(column: "_value")
                """,
                influxBucket, windowStart, now, MEASUREMENT_NAME, FIELD_ENERGY_CONSUMED, TAG_DEVICE_ID);

        return influxDBClient.getQueryApi().query(fluxQuery, influxOrg);
    }
}