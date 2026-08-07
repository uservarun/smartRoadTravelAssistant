package com.sih.roadassistant.controller;

import com.sih.roadassistant.dto.TelemetryDto;
import com.sih.roadassistant.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trips")
@CrossOrigin(origins ="*")
public class TripController {
    @Autowired
    private TripService tripService;

    @PostMapping("/telemetry")
    public ResponseEntity<Void> sendTelemetry(@RequestBody TelemetryDto telemetry){
        tripService.processTelemetry(telemetry);
        return ResponseEntity.ok().build();
    }
}
