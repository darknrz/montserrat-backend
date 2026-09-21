package com.monserrat.dto.academico;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TallerRequest {
    @NotBlank
    private String alumnoDni;

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer anio;

    @NotBlank
    private String nombre;

    @NotNull
    private BigDecimal monto;

    private Boolean pagada;

    private String observacion;
}
