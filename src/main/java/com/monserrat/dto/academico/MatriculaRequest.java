package com.monserrat.dto.academico;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MatriculaRequest {
    @NotBlank
    private String alumnoDni;

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer anio;

    private BigDecimal monto;

    @NotNull
    private Boolean pagada;

    private String observacion;
}
