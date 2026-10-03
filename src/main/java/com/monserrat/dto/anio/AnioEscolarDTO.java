package com.monserrat.dto.anio;

import com.monserrat.entity.EstadoAnioEscolar;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnioEscolarDTO {
    private Long id;
    private Integer anio;
    private EstadoAnioEscolar estado;
    private LocalDateTime fechaCierre;
    private String cerradoPor;
    private Integer totalPromovidos;
    private Integer totalRepitentes;
    private Integer totalEgresados;
    private Integer totalRetirados;
}
