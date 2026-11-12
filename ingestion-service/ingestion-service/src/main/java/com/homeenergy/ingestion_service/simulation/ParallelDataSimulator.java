package com.homeenergy.ingestion_service.simulation;

import com.homeenergy.ingestion_service.dto.EnergyUsagesDto;
import jakarta.annotation.PreDestroy;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component
public class ParallelDataSimulator implements CommandLineRunner {


    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();


    private final ExecutorService executorService;

    public ParallelDataSimulator() {
        this.executorService = Executors.newCachedThreadPool();
    }

    @Value("${simulation.parallel-threads}")
    private int parallelThreads;

    @Value("${simulation.request-per-interval}")
    private int requestPerInterval;


    @Value("${simulation.endpoint}")
    private String ingestionEndpoint;

    @Override
    public void run(String... args) throws Exception {
        log.info("ParallelDataSimulator started");
        ((ThreadPoolExecutor)executorService).setCorePoolSize(parallelThreads);
    }

    @Scheduled(fixedRateString ="${simulation.interval-ms}")
    public void sendMockData() {


        int batchSize = requestPerInterval / parallelThreads;
        int reminder  = requestPerInterval % parallelThreads;

        for( int i = 0; i < parallelThreads; i++ ) {
            int requestsForThread = batchSize + (i < reminder ? 1 : 0 );

            executorService.submit(() -> {
                for(int j = 0; j < requestsForThread; j++ ) {

                    EnergyUsagesDto dto = EnergyUsagesDto.builder()
                            .deviceId(random.nextLong(1, 6))
                            .energyConsumption(Math.round(random.nextDouble(0.0, 2.0) * 100.0) / 100.0)
                            .timestamp(Instant.now())
                            .build();
                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<EnergyUsagesDto> request = new HttpEntity<>(dto, headers);
                        restTemplate.postForEntity(ingestionEndpoint, request, Void.class);
                        log.info("Sending mock data to ingestion service: {}", dto);

                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            });
        }
    }

    @PreDestroy
    public void shoutDown(){
        executorService.shutdown();
        log.info("ParallelDataSimulator stopped");
    }

}
