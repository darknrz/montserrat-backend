package com.monserrat.dto.academico;

import com.monserrat.entity.EstadoAsistencia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaAcademicaDTO {
    private Long id;
    private Long alumnoId;
    private String alumnoDni;
    private String alumnoNombre;
    private Long docenteId;
    private String docenteDni;
    private String docenteNombre;
    private LocalDate fecha;
    private EstadoAsistencia estado;
    private String observacion;
    private LocalDateTime createdAt;
}
