package com.monserrat.dto.academico;

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
public class PensionMensualDTO {
    private String alumnoDni;
    private String alumnoCodigo;
    private String alumnoNombre;
    private NivelEducativo nivelEducativo;
    private Grado grado;
    private Seccion seccion;
    private Integer anio;
    private Integer mes;
    private Boolean pagada;
    private String observacion;
    private LocalDateTime actualizadoEn;
}
