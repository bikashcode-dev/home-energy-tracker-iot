package com.homeenergy.usage_service.service;

import com.homeenergy.kafka.event.EneragyUsageEvent;
import com.homeenergy.usage_service.model.DeviceEnergy;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UsageService {

    private final InfluxDBClient influxDBClient;

    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;

    @Autowired
    public UsageService(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    @KafkaListener(topics = "energy-usage-topic", groupId = "usage-service")
    public void energyUsageEvent(EneragyUsageEvent energyUsageEvent) {
        log.info("Received energy usage event: {}", energyUsageEvent);
        Point point = Point.measurement("energy_usage")
                .addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed", energyUsageEvent.energyConsumed())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);
        influxDBClient.getWriteApiBlocking().writePoint(influxBucket, influxOrg, point);
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() {
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minusSeconds(3600);

        List<FluxTable> tables = getFluxTables(oneHourAgo, now);
        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord recordTable : table.getRecords()) {
                String deviceIdStr = (String) recordTable.getValueByKey("deviceId");
                if (deviceIdStr == null) continue;

                double energyConsumed = recordTable.getValueByKey("_value") instanceof Number ?
                        ((Number) Objects.requireNonNull(recordTable.getValueByKey("_value"))).doubleValue() : 0.0;

                deviceEnergies.add(
                        DeviceEnergy.builder()
                                .deviceId(Long.valueOf(deviceIdStr))
                                .energyConsumed(energyConsumed)
                                .build()
                );
            }
        }
        log.info("Aggregated energy usage event: {}", deviceEnergies);
    }

    private List<FluxTable> getFluxTables(Instant oneHourAgo, Instant now) {
        String fluxQuery = String.format("""
                from(bucket: "%s")
                  |> range(start: time(v: "%s"), stop: time(v: "%s"))
                  |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                  |> filter(fn: (r) => r["_field"] == "energyConsumed")
                  |> group(columns: ["deviceId"])
                  |> sum(column: "_value")
                """,
                influxBucket, oneHourAgo.toString(), now);

        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(fluxQuery, influxOrg);
    }
}