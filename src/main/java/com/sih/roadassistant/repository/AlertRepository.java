package com.sih.roadassistant.repository;

import com.sih.roadassistant.model.Alert;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByRoadId(UUID roadId);

    // 1. Get active alerts (flood, accident, gate) within a radius
    @Query(value = "SELECT * FROM alerts WHERE is_active = true AND ST_DWithin(coordinate, :point, :radiusMeters)", nativeQuery = true)
    List<Alert> findActiveAlertsWithinRadius(@Param("point") Point point, @Param("radiusMeters") double radiusMeters);

    // 2. Scheduled job query: Auto-reopen closed gates that have exceeded closure duration
    @Modifying
    @Transactional
    @Query(value = "UPDATE alerts SET status = 'OPEN', updated_at = NOW() " +
            "WHERE alert_type = 'RAILWAY_GATE' AND status = 'CLOSED' AND updated_at < :expiryTime", nativeQuery = true)
    int expireClosedGates(@Param("expiryTime") LocalDateTime expiryTime);

    @Query(value = "SELECT ST_Distance(coordinate::geography, :point::geography) FROM alerts WHERE id = :alertId", nativeQuery = true)
    Double getDistanceToAlert(@Param("point") Point point, @Param("alertId") UUID alertId);
}