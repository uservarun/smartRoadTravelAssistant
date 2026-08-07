package com.sih.roadassistant.service;

import com.sih.roadassistant.dto.TelemetryDto;
import com.sih.roadassistant.model.Alert;
import com.sih.roadassistant.repository.AlertRepository;
import com.sih.roadassistant.util.GeometryUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TripService {

    @Autowired
    private AlertRepository alertRepository;

    private final Map<UUID, Set<UUID>> stoppedUsersPerGate = new ConcurrentHashMap<>();

    public void processTelemetry(TelemetryDto telemetry) {
        Point userLocation = GeometryUtils.createPoint(telemetry.getLatitude(), telemetry.getLongitude());

        List<Alert> nearbyAlerts = alertRepository.findActiveAlertsWithinRadius(userLocation, 50.0);

        Alert railwayGate = nearbyAlerts.stream()
                .filter(a -> "RAILWAY_GATE".equalsIgnoreCase(a.getAlertType()))
                .findFirst()
                .orElse(null);

        if (railwayGate == null) {
            return;
        }

        UUID gateId = railwayGate.getId();
        UUID userID = telemetry.getUserId();
        double speed = telemetry.getSpeedKmh();

        if (speed < 5.0) {
            stoppedUsersPerGate.computeIfAbsent(gateId, k -> Collections.synchronizedSet(new HashSet<>()))
                    .add(userID);

            Set<UUID> stoppedUsers = stoppedUsersPerGate.get(gateId);
            if (stoppedUsers.size() >= 2 && !"CLOSED".equals(railwayGate.getStatus())) {
                railwayGate.setStatus("CLOSED");
                railwayGate.setUpdatedAt(LocalDateTime.now());
                alertRepository.save(railwayGate);
                System.out.println("ALERT: Railway Gate " + gateId + " auto-flagged as CLOSED via user telemetry.");
            }
        } else if (speed > 15.0) {
            Set<UUID> stoppedUsers = stoppedUsersPerGate.get(gateId);
            if (stoppedUsers != null) {
                stoppedUsers.remove(userID);
                if (stoppedUsers.isEmpty() && "CLOSED".equals(railwayGate.getStatus())) {
                    railwayGate.setStatus("OPEN");
                    railwayGate.setUpdatedAt(LocalDateTime.now());
                    alertRepository.save(railwayGate);
                    System.out.println("ALERT: Railway Gate " + gateId + " auto-reopened as traffic is moving.");
                }
            }
        }
    }
}
