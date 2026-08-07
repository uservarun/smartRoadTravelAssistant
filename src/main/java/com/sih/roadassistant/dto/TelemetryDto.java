package com.sih.roadassistant.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TelemetryDto {
    private UUID userId;
    private UUID tripId;
    private double latitude;
    private double longitude;
    private double speedKmh;
}
