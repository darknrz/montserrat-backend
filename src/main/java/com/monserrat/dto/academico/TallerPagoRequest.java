package com.monserrat.dto.academico;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TallerPagoRequest {
    @NotBlank
    private String alumnoDni;

    @NotNull
    private Long catalogoId;

    // Monto cancelado por el alumno para ese taller (>= 0).
    @DecimalMin("0.00")
    private BigDecimal montoPagado;

    // Si se envia, fuerza el estado; si no, se deduce de montoPagado >= monto.
    private Boolean pagada;

    private String observacion;
}
