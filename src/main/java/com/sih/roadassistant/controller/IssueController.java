package com.sih.roadassistant.controller;

import com.sih.roadassistant.model.Pothole;
import com.sih.roadassistant.model.Alert;
import com.sih.roadassistant.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @PostMapping(value = "/report", consumes = "multipart/form-data")
    public ResponseEntity<Pothole> reportPothole(
            @RequestParam("userId") UUID userId,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        if (imageFile != null && !imageFile.isEmpty()) {
            com.sih.roadassistant.util.FileValidator.validateImage(imageFile);
        }

        byte[] imageBytes = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageBytes = imageFile.getBytes();
            } catch (Exception e) {
                throw new RuntimeException("Failed to read image file", e);
            }
        }

        Pothole pothole = issueService.reportPothole(userId, latitude, longitude, description, imageBytes);
        return new ResponseEntity<>(pothole, HttpStatus.CREATED);
    }

    @PostMapping("/gate")
    public ResponseEntity<Alert> updateRailwayGate(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("status") String status,
            @RequestParam("userId") UUID userId) {
        Alert alert = issueService.updateRailwayGateStatus(latitude, longitude, status, userId);
        return ResponseEntity.ok(alert);
    }

    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyHazards(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "radius", defaultValue = "5000") double radiusMeters) {
        Map<String, Object> hazards = issueService.getNearbyHazards(latitude, longitude, radiusMeters);
        return ResponseEntity.ok(hazards);
    }
}