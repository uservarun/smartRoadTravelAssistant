package com.sih.roadassistant.controller;

import com.sih.roadassistant.dto.RouteRequest;
import com.sih.roadassistant.service.RoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/routing")
@CrossOrigin(origins = "*")
public class RoutingController {

    @Autowired
    private RoutingService routingService;

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateSafeRoute(@RequestBody RouteRequest request) {
        Map<String, Object> result = routingService.calculateSafeRoute(request);
        return ResponseEntity.ok(result);
    }
}