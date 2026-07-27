package com.monserrat.repository;

import com.monserrat.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUserTypeAndUserIdAndUsedAtIsNull(String userType, Long userId);

    void deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
