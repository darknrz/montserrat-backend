package com.monserrat.dto.academico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodoBimestreDTO {
    private Long id;
    private Integer anio;
    private Integer numeroBimestre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
