package com.monserrat.dto.anio;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MigracionRequest {
    private Integer anioDestino;
    /** Copia las fechas de los bimestres del año de origen (desplazadas) si el destino no tiene. */
    private Boolean copiarBimestres = true;
    private List<MigracionDecisionDTO> decisiones = new ArrayList<>();
    /** Solo en la ejecución: debe ser exactamente "MIGRAR {anioDestino}". */
    private String confirmacion;
}
