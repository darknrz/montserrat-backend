package com.monserrat.dto.academico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicoConfigDTO {
    @Builder.Default
    private List<CatalogItemDTO> cursosPrimaria = new ArrayList<>();
    @Builder.Default
    private List<CatalogItemDTO> cursosSecundaria = new ArrayList<>();
    @Builder.Default
    private List<CatalogItemDTO> gradosPrimaria = new ArrayList<>();
    @Builder.Default
    private List<CatalogItemDTO> gradosSecundaria = new ArrayList<>();
    @Builder.Default
    private List<CatalogItemDTO> seccionesPrimaria = new ArrayList<>();
    @Builder.Default
    private List<CatalogItemDTO> seccionesSecundaria = new ArrayList<>();
    @Builder.Default
    private List<SalonItemDTO> salones = new ArrayList<>();
    @Builder.Default
    private Integer minAsistenciaPorcentaje = 70;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CatalogItemDTO {
        private String id;
        private String label;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalonItemDTO {
        private String nivel;
        private String grado;
        private String seccion;
        private String aula;
        private Boolean active;
    }
}
