package com.sih.roadassistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType; // 'RAILWAY_GATE', 'WATER_LOGGING', 'ACCIDENT', 'WEATHER'

    @Column(nullable = false, length = 50)
    private String status; // 'OPEN', 'CLOSED', 'CLOSING_SOON'

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point coordinate;

    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "road_id")
    private Road road;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}