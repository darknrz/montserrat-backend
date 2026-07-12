package com.monserrat.dto.academico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportacionResultDTO {
    private int totalProcesados;
    private int exitosos;
    private int fallidos;
    private List<String> errores;
    private String mensaje;
}
