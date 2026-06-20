package com.monserrat.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "La contrasena actual es obligatoria")
    private String currentPassword;

    @NotBlank(message = "La nueva contrasena es obligatoria")
    private String newPassword;
}
