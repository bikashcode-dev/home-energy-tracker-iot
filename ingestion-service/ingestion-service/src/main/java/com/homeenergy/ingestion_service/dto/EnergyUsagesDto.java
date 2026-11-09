package com.homeenergy.ingestion_service.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


@Data
public class EnergyUsagesDto {

    private Long deviceId;
    double energyConsumption;
    @JsonFormat(shape = JsonFormat.Shape.STRING)

}
