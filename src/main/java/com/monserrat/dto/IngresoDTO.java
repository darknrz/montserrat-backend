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
public class IngresoDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La universidad es obligatoria")
    private String universidad;

    @NotBlank(message = "Las siglas son obligatorias")
    private String universidadSiglas;

    @NotBlank(message = "La carrera es obligatoria")
    private String carrera;

    @NotBlank(message = "El año es obligatorio")
    private String anio;

    @NotBlank(message = "El tipo de selección es obligatorio")
    private String tipoSeleccion;

    private String fotoUrl;
    private Boolean activo;
}