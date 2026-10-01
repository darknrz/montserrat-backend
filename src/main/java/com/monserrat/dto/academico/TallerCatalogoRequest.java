package com.monserrat.dto.academico;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TallerCatalogoRequest {
    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer anio;

    @NotBlank
    private String nombre;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal monto;

    // "SALON:CICLADO I" | "GRADO:SEGUNDO_SECUNDARIA"
    private List<String> aplicaA;
}
