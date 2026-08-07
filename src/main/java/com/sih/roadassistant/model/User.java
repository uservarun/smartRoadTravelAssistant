package com.sih.roadassistant.model;

import com.sih.roadassistant.security.EncryptionConverter;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length =255)
    @Convert(converter = EncryptionConverter.class)
    private String email;

    @Column(name ="password_hash", nullable = false)
    @JsonIgnore
    private String passwordHash;

    @Column(length = 20)
    @Builder.Default
    private String role = "USER";
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verification_code", length = 6)
    @JsonIgnore
    private String verificationCode;
}
