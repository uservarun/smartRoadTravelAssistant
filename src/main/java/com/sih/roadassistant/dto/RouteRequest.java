package com.sih.roadassistant.dto;

import lombok.Data;
import org.locationtech.jts.geom.Coordinate;

@Data
public class RouteRequest {
    private CoordinateDto startLocation;
    private CoordinateDto endLocation;
    private String safetyPreference;
}
