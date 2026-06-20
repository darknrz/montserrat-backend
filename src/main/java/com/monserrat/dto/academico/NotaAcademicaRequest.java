package com.monserrat.dto.academico;

import com.monserrat.entity.CursoAcademico;
import com.monserrat.entity.TipoEvaluacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class NotaAcademicaRequest {
    @NotBlank(message = "El DNI del alumno es obligatorio")
    private String alumnoDni;

    @NotNull(message = "El curso es obligatorio")
    private CursoAcademico curso;

    @NotBlank(message = "El periodo es obligatorio")
    private String periodo;

    @NotNull(message = "El tipo de evaluacion es obligatorio")
    private TipoEvaluacion tipoEvaluacion;

    @NotNull(message = "La nota es obligatoria")
    @PositiveOrZero
    private Double valor;

    private String observacion;
}
