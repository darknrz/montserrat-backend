package com.monserrat.dto.academico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TallerCatalogoDTO {
    private Long id;
    private Integer anio;
    private String nombre;
    private BigDecimal monto;
    private List<String> aplicaA;
}
