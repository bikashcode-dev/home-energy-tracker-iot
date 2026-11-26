package com.homeenergy.kafka.event;

import lombok.Builder;

@Builder
public record AlertingEvent(
    Long userId ,
    String message,
    double threshold,
    double emergyConsumed,
    String email
){}
