package com.sih.roadassistant.service;

import com.sih.roadassistant.model.*;
import com.sih.roadassistant.repository.*;
import com.sih.roadassistant.util.GeometryUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

@Service
public class IssueService {

    @Autowired
    private PotholeRepository potholeRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private RoadRepository roadRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<UUID, Instant> userReportCooldowns = new ConcurrentHashMap<>();

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    public Pothole reportPothole(UUID userId, double lat, double lng, String description, byte[] imageBytes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Rate limit (5 minutes cooldown per user)
        enforceUserCooldown(userId);

        Point coordinate = GeometryUtils.createPoint(lat, lng);
        Road nearestRoad = roadRepository.findNearestRoad(coordinate);

        Pothole pothole = Pothole.builder()
                .coordinate(coordinate)
                .severity("MEDIUM")
                .aiStatus("PENDING")
                .road(nearestRoad)
                .build();

        Pothole savedPothole = potholeRepository.save(pothole);

        Report report = Report.builder()
                .user(user)
                .reportType("POTHOLE")
                .pothole(savedPothole)
                .build();
        reportRepository.save(report);

        if (imageBytes != null && imageBytes.length > 0 && !geminiApiKey.isEmpty()) {
            verifyPotholeWithAI(savedPothole.getId(), imageBytes);
        }

        // Record cooldown
        userReportCooldowns.put(userId, Instant.now());

        return savedPothole;
    }

    public Alert updateRailwayGateStatus(double lat, double lng, String gateStatus, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Rate Limit (5 minutes cooldown)
        enforceUserCooldown(userId);

        Point coordinate = GeometryUtils.createPoint(lat, lng);
        Road nearestRoad = roadRepository.findNearestRoad(coordinate);

        List<Alert> nearbyAlerts = alertRepository.findActiveAlertsWithinRadius(coordinate, 50.0);
        Alert gateAlert = nearbyAlerts.stream()
                .filter(a -> "RAILWAY_GATE".equalsIgnoreCase(a.getAlertType()))
                .findFirst()
                .orElse(null);

        if (gateAlert != null) {
            Double distance = alertRepository.getDistanceToAlert(coordinate, gateAlert.getId());
            if (distance == null || distance > 50.0) { // 50 meters
                throw new RuntimeException("Verification failed: You must be within 50 meters of the crossing to report status.");
            }

            gateAlert.setStatus(gateStatus.toUpperCase());
            gateAlert.setUpdatedAt(LocalDateTime.now());

            userReportCooldowns.put(userId, Instant.now());
            return alertRepository.save(gateAlert);
        } else {
            // Create a new railway gate alert if none exists nearby
            Alert newGate = Alert.builder()
                    .alertType("RAILWAY_GATE")
                    .status(gateStatus.toUpperCase())
                    .coordinate(coordinate)
                    .description("Railway Crossing Gate")
                    .road(nearestRoad)
                    .isActive(true)
                    .build();

            userReportCooldowns.put(userId, Instant.now());
            return alertRepository.save(newGate);
        }
    }

    public Map<String, Object> getNearbyHazards(double lat, double lng, double radiusMeters) {
        Point point = GeometryUtils.createPoint(lat, lng);
        List<Pothole> potholes = potholeRepository.findPotholesWithinRadius(point, radiusMeters);
        List<Alert> alerts = alertRepository.findActiveAlertsWithinRadius(point, radiusMeters);

        Map<String, Object> hazards = new HashMap<>();
        hazards.put("potholes", potholes);
        hazards.put("alerts", alerts);
        return hazards;
    }

    private void enforceUserCooldown(UUID userId) {
        Instant lastReport = userReportCooldowns.get(userId);
        if (lastReport != null) {
            long secondsPassed = Instant.now().getEpochSecond() - lastReport.getEpochSecond();
            if (secondsPassed < 300) { // 5 minutes (300 seconds)
                long secondsRemaining = 300 - secondsPassed;
                throw new RuntimeException("Please wait " + secondsRemaining + " seconds before submitting another report.");
            }
        }
    }

    @Async
    public CompletableFuture<Void> verifyPotholeWithAI(UUID potholeId, byte[] imageBytes) {
        return CompletableFuture.runAsync(() -> {
            try {
                Pothole pothole = potholeRepository.findById(potholeId).orElse(null);
                if (pothole == null) return;

                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                String geminiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

                Map<String, Object> inlineData = new HashMap<>();
                inlineData.put("mimeType", "image/jpeg");
                inlineData.put("data", base64Image);

                Map<String, Object> part1 = new HashMap<>();
                part1.put("text", "Does this image show a pothole, open manhole, or road fracture? " +
                        "Answer in JSON only: {\"isPothole\": true/false, \"severity\": \"LOW/MEDIUM/HIGH\"}");

                Map<String, Object> part2 = new HashMap<>();
                part2.put("inlineData", inlineData);

                Map<String, Object> contentPart = new HashMap<>();
                contentPart.put("parts", List.of(part1, part2));

                Map<String, Object> payload = new HashMap<>();
                payload.put("contents", List.of(contentPart));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(geminiUrl, requestEntity, String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    String aiTextResponse = root.path("candidates").get(0)
                            .path("content").path("parts").get(0).path("text").asText().trim();

                    if (aiTextResponse.startsWith("```json")) {
                        aiTextResponse = aiTextResponse.substring(7, aiTextResponse.length() - 3).trim();
                    }

                    JsonNode aiJson = objectMapper.readTree(aiTextResponse);
                    boolean isPothole = aiJson.path("isPothole").asBoolean();
                    String severity = aiJson.path("severity").asText("MEDIUM").toUpperCase();

                    if (isPothole) {
                        pothole.setAiStatus("VERIFIED");
                        pothole.setSeverity(severity);
                    } else {
                        pothole.setAiStatus("FALSE_ALARM");
                    }
                    potholeRepository.save(pothole);
                }
            } catch (Exception e) {
                System.err.println("Gemini AI pothole verification failed: " + e.getMessage());
            }
        });
    }
}