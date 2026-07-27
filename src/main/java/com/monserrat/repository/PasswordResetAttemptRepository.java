package com.monserrat.repository;

import com.monserrat.entity.PasswordResetAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PasswordResetAttemptRepository extends JpaRepository<PasswordResetAttempt, Long> {
    long countByEmailAndRequestedAtGreaterThanEqual(String email, LocalDateTime requestedAt);

    void deleteByRequestedAtBefore(LocalDateTime requestedAt);
}
