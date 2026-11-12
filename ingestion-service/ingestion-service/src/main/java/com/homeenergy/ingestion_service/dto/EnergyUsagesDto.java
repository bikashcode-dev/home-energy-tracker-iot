package com.homeenergy.ingestion_service.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;


import java.time.Instant;

@Builder
public record EnergyUsagesDto(
        Long deviceId,
        double energyConsumption,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp){

}


