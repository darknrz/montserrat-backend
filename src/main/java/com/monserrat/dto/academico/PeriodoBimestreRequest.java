package com.monserrat.dto.academico;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodoBimestreRequest {

    @NotNull(message = "El año es requerido")
    @Min(2000)
    @Max(2100)
    private Integer anio;

    @NotNull(message = "El número de bimestre es requerido")
    @Min(1)
    @Max(4)
    private Integer numeroBimestre;

    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es requerida")
    private LocalDate fechaFin;
}
