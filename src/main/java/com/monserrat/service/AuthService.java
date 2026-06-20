package com.monserrat.service;

import com.monserrat.dto.auth.LoginRequest;
import com.monserrat.dto.auth.LoginResponse;
import com.monserrat.entity.Admin;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.repository.AdminRepository;
import com.monserrat.repository.UsuarioAcademicoRepository;
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
    private final UsuarioAcademicoRepository usuarioAcademicoRepository;
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

        return adminRepository.findByUsername(request.getUsername())
                .map(admin -> buildAdminResponse(token, admin))
                .or(() -> usuarioAcademicoRepository.findByDni(request.getUsername())
                        .map(usuario -> buildAcademicResponse(token, usuario)))
                .orElseThrow();
    }

    private LoginResponse buildAdminResponse(String token, Admin admin) {
        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .userId(admin.getId())
                .username(admin.getUsername())
                .nombre(admin.getNombre())
                .rol(admin.getRol())
                .debeCambiarContrasena(false)
                .build();
    }

    private LoginResponse buildAcademicResponse(String token, UsuarioAcademico usuario) {
        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .userId(usuario.getId())
                .username(usuario.getDni())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .debeCambiarContrasena(usuario.getDebeCambiarContrasena())
                .build();
    }
}
