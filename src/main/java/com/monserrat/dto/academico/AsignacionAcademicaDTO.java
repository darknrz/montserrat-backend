package com.monserrat.dto.academico;

import com.monserrat.entity.CursoAcademico;
import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.Seccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionAcademicaDTO {
    private Long id;
    private Long docenteId;
    private String docenteDni;
    private String docenteNombre;
    private Long alumnoId;
    private String alumnoDni;
    private String alumnoNombre;
    private CursoAcademico curso;
    private NivelEducativo nivelEducativo;
    private Grado grado;
    private Seccion seccion;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
