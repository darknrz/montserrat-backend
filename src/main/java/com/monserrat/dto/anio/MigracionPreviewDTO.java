package com.monserrat.dto.anio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigracionPreviewDTO {
    private Integer anioOrigen;
    private Integer anioDestino;
    private List<MigracionItemDTO> items;
    private int promovidos;
    private int repitentes;
    private int egresados;
    private int retirados;
    /** Alumnos que aún necesitan sección de destino. */
    private int pendientesSeccion;
    private int notasAArchivar;
    private int asistenciasAArchivar;
    private int bimestresACopiar;
    /** Alumnos activos que no se migran (sin grado o con estado distinto de MATRICULADO). */
    private List<String> omitidos;
    private boolean puedeEjecutar;
}
