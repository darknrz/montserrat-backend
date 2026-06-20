package com.monserrat.dto.academico;

import com.monserrat.entity.CursoAcademico;
import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.Seccion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignacionAulaRequest {
    @NotBlank(message = "El DNI del docente es obligatorio")
    private String docenteDni;

    private CursoAcademico curso;

    @NotNull(message = "El nivel educativo es obligatorio")
    private NivelEducativo nivelEducativo;

    @NotNull(message = "El grado es obligatorio")
    private Grado grado;

    @NotNull(message = "La seccion es obligatoria")
    private Seccion seccion;

    private Boolean activo;
}
