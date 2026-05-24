package com.monserrat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    private String distrito;

    @NotBlank(message = "El año de fundación es obligatorio")
    private String anioFundacion;

    private String telefono;

    @Email(message = "Email inválido")
    private String email;

    private String niveles;
    private String tipo;

    @NotBlank(message = "La misión es obligatoria")
    private String mision;

    @NotBlank(message = "La visión es obligatoria")
    private String vision;

    private String descripcion;
    private String logoUrl;
    private String horarioAtencion;
}