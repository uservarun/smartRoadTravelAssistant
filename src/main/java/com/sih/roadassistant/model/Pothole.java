package com.sih.roadassistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "potholes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pothole {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point coordinate;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(name = "ai_status", length = 20)
    @Builder.Default
    private String aiStatus = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "road_id")
    private Road road;

    @Column(name = "detected_at", insertable = false, updatable = false)
    private LocalDateTime detectedAt;
}