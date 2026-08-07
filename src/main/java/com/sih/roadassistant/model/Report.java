package com.sih.roadassistant.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType; // 'POTHOLE', 'ALERT'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pothole_id")
    private Pothole pothole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private Alert alert;

    @Column(name = "reported_at", insertable = false, updatable = false)
    private LocalDateTime reportedAt;
}