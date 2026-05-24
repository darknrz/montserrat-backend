package com.monserrat.service;

import com.monserrat.dto.auth.LoginRequest;
import com.monserrat.dto.auth.LoginResponse;
import com.monserrat.entity.Admin;
import com.monserrat.repository.AdminRepository;
import com.monserrat.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contrasena incorrectos");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow();

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .username(admin.getUsername())
                .nombre(admin.getNombre())
                .rol(admin.getRol())
                .build();
    }
}
