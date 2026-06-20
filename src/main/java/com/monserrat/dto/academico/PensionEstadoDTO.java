package com.monserrat.dto.academico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PensionEstadoDTO {
    private String dni;
    private String nombre;
    private Boolean pagada;
    private String observacion;
    private LocalDateTime actualizadoEn;
}
