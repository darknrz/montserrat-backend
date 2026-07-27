package com.monserrat.service;

import com.monserrat.dto.auth.ForgotPasswordRequest;
import com.monserrat.dto.auth.ResetPasswordRequest;
import com.monserrat.entity.Admin;
import com.monserrat.entity.PasswordResetAttempt;
import com.monserrat.entity.PasswordResetToken;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.repository.AdminRepository;
import com.monserrat.repository.PasswordResetAttemptRepository;
import com.monserrat.repository.PasswordResetTokenRepository;
import com.monserrat.repository.UsuarioAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String USER_TYPE_ADMIN = "ADMIN";
    private static final String USER_TYPE_ACADEMICO = "ACADEMICO";
    private static final int TOKEN_BYTES = 32;
    private static final long TOKEN_MINUTES = 30;
    private static final int MAX_DAILY_EMAIL_ATTEMPTS = 3;

    private final AdminRepository adminRepository;
    private final UsuarioAcademicoRepository usuarioAcademicoRepository;
    private final PasswordResetAttemptRepository attemptRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResendEmailService resendEmailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        String email = normalize(request.getEmail());
        registerDailyAttempt(email);

        Optional<UsuarioAcademico> academico = usuarioAcademicoRepository.findByCorreoIgnoreCase(email);
        if (academico.isPresent() && Boolean.TRUE.equals(academico.get().getActivo())) {
            createAndSendToken(USER_TYPE_ACADEMICO, academico.get().getId(), email);
            return;
        }

        Optional<Admin> admin = adminRepository.findByUsername(email);
        if (admin.isPresent() && Boolean.TRUE.equals(admin.get().getActivo()) && looksLikeEmail(admin.get().getUsername())) {
            createAndSendToken(USER_TYPE_ADMIN, admin.get().getId(), admin.get().getUsername());
        }
    }

    private void registerDailyAttempt(String email) {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        long attemptsToday = attemptRepository.countByEmailAndRequestedAtGreaterThanEqual(email, todayStart);
        if (attemptsToday >= MAX_DAILY_EMAIL_ATTEMPTS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Intentos maximos alcanzados. Intentalo manana.");
        }

        attemptRepository.save(PasswordResetAttempt.builder()
                .email(email)
                .requestedAt(LocalDateTime.now())
                .build());
        attemptRepository.deleteByRequestedAtBefore(todayStart.minusDays(7));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(normalize(request.getToken()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalido o vencido"));

        LocalDateTime now = LocalDateTime.now();
        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalido o vencido");
        }

        if (USER_TYPE_ADMIN.equals(resetToken.getUserType())) {
            Admin admin = adminRepository.findById(resetToken.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalido o vencido"));
            if (passwordEncoder.matches(request.getNewPassword(), admin.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contrasena debe ser diferente");
            }
            admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
            adminRepository.save(admin);
        } else if (USER_TYPE_ACADEMICO.equals(resetToken.getUserType())) {
            UsuarioAcademico usuario = usuarioAcademicoRepository.findById(resetToken.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalido o vencido"));
            if (passwordEncoder.matches(request.getNewPassword(), usuario.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contrasena debe ser diferente");
            }
            usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
            usuario.setDebeCambiarContrasena(false);
            usuarioAcademicoRepository.save(usuario);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalido o vencido");
        }

        resetToken.setUsedAt(now);
        tokenRepository.save(resetToken);
        tokenRepository.deleteByExpiresAtBefore(now.minusDays(1));
    }

    private void createAndSendToken(String userType, Long userId, String email) {
        tokenRepository.deleteByUserTypeAndUserIdAndUsedAtIsNull(userType, userId);
        String token = generateToken();
        tokenRepository.save(PasswordResetToken.builder()
                .token(token)
                .userType(userType)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_MINUTES))
                .build());

        String resetUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/restablecer-password")
                .queryParam("token", token)
                .build()
                .toUriString();
        resendEmailService.sendPasswordResetEmail(email, resetUrl);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean looksLikeEmail(String value) {
        return value != null && value.contains("@") && value.contains(".");
    }
}
