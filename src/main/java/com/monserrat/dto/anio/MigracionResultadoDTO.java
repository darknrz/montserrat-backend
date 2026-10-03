package com.monserrat.dto.anio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigracionResultadoDTO {
    private Integer anioOrigen;
    private Integer anioDestino;
    private int promovidos;
    private int repitentes;
    private int egresados;
    private int retirados;
    private int notasArchivadas;
    private int asistenciasArchivadas;
    private int bimestresCopiados;
    /** Alumnos que quedaron sin docentes asignados y requieren asignación de aula/docente. */
    private int alumnosSinAsignaciones;
}
