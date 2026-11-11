package com.homeenergy.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.Instant;

@Builder
public record EneragyUsageEvent(Long deviceId, double energyConsumed,
                                @JsonFormat(shape = JsonFormat.Shape.STRING)
                                 Instant timestamp){

}