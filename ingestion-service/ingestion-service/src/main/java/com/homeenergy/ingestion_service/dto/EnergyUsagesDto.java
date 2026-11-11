package com.homeenergy.ingestion_service.dto;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;


public record EnergyUsagesDto(Long deviceId,
                              double energyConsumption,
                              @JsonFormat(shape = JsonFormat.Shape.STRING)
                              Instant timestamp){

}


