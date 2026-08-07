package com.sih.roadassistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.LineString;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_point", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point startPoint;

    @Column(name = "end_point", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point endPoint;

    @Column(name = "path_geometry", nullable = false, columnDefinition = "geometry(LineString, 4326)")
    private LineString pathGeometry;

    @Column(name = "safety_score", nullable = false)
    private Double safetyScore;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}