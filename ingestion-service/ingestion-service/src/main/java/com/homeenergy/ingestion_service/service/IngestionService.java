package com.homeenergy.ingestion_service.service;
import com.homeenergy.ingestion_service.dto.EnergyUsagesDto;
import com.homeenergy.kafka.event.EneragyUsageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class IngestionService {

    private final KafkaTemplate<String, EneragyUsageEvent> kafkaTemplate;

    public IngestionService(KafkaTemplate<String, EneragyUsageEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public  void ingestEnergyUsage(EnergyUsagesDto input){
        // Convert DTO to Event

        EneragyUsageEvent event = EneragyUsageEvent.builder()
                .deviceId(input.deviceId())
                .energyConsumed(input.energyConsumption())
                .timestamp(input.timestamp())
                .build();
        // Send the event to kafka topic
        kafkaTemplate.send("energy-usage-topic", event);
        log.info("Sent energy-usage event", event);

    }
}
