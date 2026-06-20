package com.monserrat.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tipo;
    private Long userId;
    private String username;
    private String nombre;
    private String rol;
    private Boolean debeCambiarContrasena;
}
