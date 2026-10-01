package com.monserrat.dto.academico;

import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.Seccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TallerDTO {
    private Long id;
    private Long catalogoId;
    private BigDecimal montoPagado;
    private String alumnoDni;
    private String alumnoCodigo;
    private String alumnoNombre;
    private NivelEducativo nivelEducativo;
    private Grado grado;
    private Seccion seccion;
    private Integer anio;
    private String nombre;
    private BigDecimal monto;
    private Boolean pagada;
    private String observacion;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
