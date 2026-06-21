package com.monserrat.dto.academico;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PensionMensualRequest {
    @NotBlank
    private String alumnoDni;

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer anio;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer mes;

    @NotNull
    private Boolean pagada;

    private String observacion;
}
