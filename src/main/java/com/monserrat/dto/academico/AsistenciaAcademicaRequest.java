package com.monserrat.dto.academico;

import com.monserrat.entity.EstadoAsistencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AsistenciaAcademicaRequest {
    @NotBlank(message = "El DNI del alumno es obligatorio")
    private String alumnoDni;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "El estado es obligatorio")
    private EstadoAsistencia estado;

    private String observacion;
}
