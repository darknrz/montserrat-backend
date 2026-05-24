package com.monserrat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedSocialDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El ícono es obligatorio")
    private String icono;

    @NotBlank(message = "La URL es obligatoria")
    private String url;

    private Boolean activo;
    private Integer orden;
}