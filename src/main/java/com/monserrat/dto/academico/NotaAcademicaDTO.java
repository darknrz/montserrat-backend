package com.monserrat.dto.academico;

import com.monserrat.entity.CursoAcademico;
import com.monserrat.entity.TipoEvaluacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaAcademicaDTO {
    private Long id;
    private Long alumnoId;
    private String alumnoDni;
    private String alumnoNombre;
    private Long docenteId;
    private String docenteDni;
    private String docenteNombre;
    private CursoAcademico curso;
    private String periodo;
    private TipoEvaluacion tipoEvaluacion;
    private Double valor;
    private String observacion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
