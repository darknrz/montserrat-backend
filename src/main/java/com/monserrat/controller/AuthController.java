package com.monserrat.controller;

import com.monserrat.dto.auth.LoginRequest;
import com.monserrat.dto.auth.LoginResponse;
import com.monserrat.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Body: { "username": "adminmontserrat", "password": "adminmontserrat" }
     * Response: { "token": "...", "tipo": "Bearer", "username": "...", "nombre": "...", "rol": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/verify
     * Verifica si el token actual sigue siendo válido (requiere Authorization header)
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verify() {
        return ResponseEntity.ok("Token válido");
    }
}
