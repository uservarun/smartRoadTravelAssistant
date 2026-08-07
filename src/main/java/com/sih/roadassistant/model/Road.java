package com.sih.roadassistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.LineString;
import java.util.UUID;

@Entity
@Table(name = "roads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Road {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "road_name", length = 100)
    private String roadName;

    @Column(nullable = false, columnDefinition = "geometry(LineString, 4326)")
    private LineString geometry;

    @Column(name = "length_m", nullable = false)
    private Double lengthM;

    @Column(nullable = false)
    @Builder.Default
    private Double curvature = 0.0;

    @Column(name = "speed_limit", nullable = false)
    private Double speedLimit;

    @Column(name = "avg_speed", nullable = false)
    private Double avgSpeed;

    @Column(name = "speed_variance", nullable = false)
    @Builder.Default
    private Double speedVariance = 0.0;

    @Column(name = "risk_index", nullable = false)
    @Builder.Default
    private Double riskIndex = 0.0;

    @Column(name = "risk_class", nullable = false)
    @Builder.Default
    private Integer riskClass = 1;
}