package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_attempts", indexes = {
        @Index(name = "idx_password_reset_attempts_email_requested_at", columnList = "email, requested_at"),
        @Index(name = "idx_password_reset_attempts_requested_at", columnList = "requested_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @PrePersist
    private void ensureRequestedAt() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }
}
