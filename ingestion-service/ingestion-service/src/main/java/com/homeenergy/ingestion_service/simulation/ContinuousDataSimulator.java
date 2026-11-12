package com.homeenergy.ingestion_service.simulation;


import com.homeenergy.ingestion_service.dto.EnergyUsagesDto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Random;

@Slf4j
@Component
public class ContinuousDataSimulator  implements CommandLineRunner {

    private final  RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${simulation.request-per-interval}")
    private int requestPerInterval;


    @Value("${simulation.endpoint}")
    private String ingestionEndPoint;

    @Override
    public void run(String... args) throws Exception {
        log.info("Simulation started");
    }

    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData() {
        for (int i = 0; i < requestPerInterval; i++) {
            EnergyUsagesDto dto =  EnergyUsagesDto.builder()
                    .deviceId(random.nextLong(1,6))
                    .energyConsumption(Math.round(random.nextDouble(0.0,10.0)*100.0 /100.0))
                    .timestamp(Instant.now())
                    .build();
            try{
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<EnergyUsagesDto> request = new HttpEntity<>(dto, headers);
                restTemplate.postForEntity(ingestionEndPoint, request, Void.class);

                log.info("Mock Data sent to ingestion {} ", dto);

            }
            catch (Exception e){
                log.error("Failed to send Data : {}", e.getMessage());
            }

        }
    }
}
