package com.homeenergy.usage_service.service;

import com.homeenergy.kafka.event.EneragyUsageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsageService {

    @KafkaListener(topics = "energy-usage-topic", groupId = "usage-service")
    public void energyUsageEvent(EneragyUsageEvent energyUsageEvent) {
        log.info("Received energy usage event: {}", energyUsageEvent);
    }
}
