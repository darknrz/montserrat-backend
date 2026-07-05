package com.monserrat.service;

import com.monserrat.dto.academico.AcademicoConfigDTO;
import com.monserrat.entity.CatalogoAcademico;
import com.monserrat.entity.SalonAcademico;
import com.monserrat.repository.CatalogoAcademicoRepository;
import com.monserrat.repository.SalonAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicoConfigService {

    private static final int MAX_CATALOGO_NOMBRE_LENGTH = 500;

    private final CatalogoAcademicoRepository catalogoRepository;
    private final SalonAcademicoRepository salonRepository;

    @Transactional(readOnly = true)
    public AcademicoConfigDTO obtener() {
        AcademicoConfigDTO dto = new AcademicoConfigDTO();
        Map<String, List<String>> competenciasPorCurso = new LinkedHashMap<>();
        Map<String, String> docentesPorCompetencia = new LinkedHashMap<>();

        catalogoRepository.findAllByOrderByOrdenAscIdAsc().forEach(item -> {
            addCatalog(dto, item);
            if ("COMPETENCIA_CURSO".equals(item.getTipo()) && "PRIMARIA".equals(item.getNivel())) {
                competenciasPorCurso.put(item.getCodigo(), parseCsvList(item.getNombre()));
            }
            if ("DOCENTE_COMPETENCIA".equals(item.getTipo()) && "PRIMARIA".equals(item.getNivel())) {
                docentesPorCompetencia.put(item.getCodigo(), item.getNombre());
            }
        });

        dto.setCompetenciasPorCursoPrimaria(competenciasPorCurso);
        dto.setDocentesPorCompetencia(docentesPorCompetencia);
        dto.setSalones(salonRepository.findAllByOrderByOrdenAscIdAsc().stream().map(this::toSalonDto).toList());
        return dto;
    }

    @Transactional
    public AcademicoConfigDTO guardar(AcademicoConfigDTO request) {
        catalogoRepository.deleteAllInBatch();
        salonRepository.deleteAllInBatch();

        List<CatalogoAcademico> catalogos = new ArrayList<>();
        // Primaria usa "AREA_CURRICULAR" (término correcto del currículo peruano
        // para ese nivel); secundaria usa "CURSO". Mantener esta distinción
        // consistente en TODO el flujo: DataInitializer, guardar() y el switch
        // de addCatalog() de abajo deben usar siempre las mismas dos etiquetas.
        addCatalogos(catalogos, "AREA_CURRICULAR", "PRIMARIA", request.getCursosPrimaria());
        addCatalogos(catalogos, "CURSO", "SECUNDARIA", request.getCursosSecundaria());
        addCatalogos(catalogos, "GRADO", "PRIMARIA", request.getGradosPrimaria());
        addCatalogos(catalogos, "COMPETENCIA", "PRIMARIA", request.getCompetenciasPrimaria());
        addCatalogos(catalogos, "GRADO", "SECUNDARIA", request.getGradosSecundaria());
        addCatalogos(catalogos, "SECCION", "PRIMARIA", request.getSeccionesPrimaria());
        addCatalogos(catalogos, "SECCION", "SECUNDARIA", request.getSeccionesSecundaria());
        addCompetenciasPorCurso(catalogos, request.getCompetenciasPorCursoPrimaria());
        addDocentesPorCompetencia(catalogos, request.getDocentesPorCompetencia());
        catalogoRepository.saveAll(catalogos);

        Integer minPct = request.getMinAsistenciaPorcentaje() != null ? request.getMinAsistenciaPorcentaje() : 70;
        catalogoRepository.save(CatalogoAcademico.builder()
                .tipo("ASISTENCIA")
                .nivel("GLOBAL")
                .codigo("MIN_PORCENTAJE")
                .nombre(minPct.toString())
                .activo(true)
                .orden(999)
                .build());

        String modelo = request.getIngresantesModelo() == null ? "card-grid" : request.getIngresantesModelo();
        catalogoRepository.save(CatalogoAcademico.builder()
            .tipo("INGRESANTES")
            .nivel("GLOBAL")
            .codigo("MODEL")
            .nombre(modelo)
            .activo(true)
            .orden(998)
            .build());

        List<SalonAcademico> salones = new ArrayList<>();
        List<AcademicoConfigDTO.SalonItemDTO> salonItems = request.getSalones() == null ? List.of() : request.getSalones();
        for (int i = 0; i < salonItems.size(); i++) {
            AcademicoConfigDTO.SalonItemDTO item = salonItems.get(i);
            salones.add(SalonAcademico.builder()
                    .nivel(item.getNivel())
                    .grado(item.getGrado())
                    .seccion(item.getSeccion())
                    .aula(item.getAula())
                    .activo(item.getActive() == null || item.getActive())
                    .orden(i)
                    .build());
        }
        salonRepository.saveAll(salones);
        return obtener();
    }

    private void addCatalogos(List<CatalogoAcademico> target, String tipo, String nivel, List<AcademicoConfigDTO.CatalogItemDTO> items) {
        List<AcademicoConfigDTO.CatalogItemDTO> safeItems = items == null ? List.of() : items;
        Map<String, AcademicoConfigDTO.CatalogItemDTO> uniqueItems = new LinkedHashMap<>();

        for (AcademicoConfigDTO.CatalogItemDTO item : safeItems) {
            if (item == null) {
                continue;
            }
            String label = item.getLabel() == null ? "" : item.getLabel();
            if (label.length() > MAX_CATALOGO_NOMBRE_LENGTH) {
                throw new IllegalArgumentException("El texto no puede superar 500 caracteres");
            }
            String id = item.getId() == null ? "" : item.getId().trim();
            if (id.isBlank()) {
                continue;
            }
            uniqueItems.putIfAbsent(id, item);
        }

        int index = 0;
        for (AcademicoConfigDTO.CatalogItemDTO item : uniqueItems.values()) {
            target.add(CatalogoAcademico.builder()
                    .tipo(tipo)
                    .nivel(nivel)
                    .codigo(item.getId())
                    .nombre(item.getLabel())
                    .activo(item.getActive() == null || item.getActive())
                    .orden(index++)
                    .build());
        }
    }

    private void addCompetenciasPorCurso(List<CatalogoAcademico> target, Map<String, List<String>> mappings) {
        if (mappings == null || mappings.isEmpty()) return;
        int index = 1000;
        for (Map.Entry<String, List<String>> entry : mappings.entrySet()) {
            target.add(CatalogoAcademico.builder()
                    .tipo("COMPETENCIA_CURSO")
                    .nivel("PRIMARIA")
                    .codigo(entry.getKey())
                    .nombre(serializeList(entry.getValue()))
                    .activo(true)
                    .orden(index++)
                    .build());
        }
    }

    private void addDocentesPorCompetencia(List<CatalogoAcademico> target, Map<String, String> mappings) {
        if (mappings == null || mappings.isEmpty()) return;
        int index = 2000;
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            target.add(CatalogoAcademico.builder()
                    .tipo("DOCENTE_COMPETENCIA")
                    .nivel("PRIMARIA")
                    .codigo(entry.getKey())
                    .nombre(entry.getValue())
                    .activo(true)
                    .orden(index++)
                    .build());
        }
    }

    private String serializeList(List<String> values) {
        return values == null ? "" : values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.joining(","));
    }

    private List<String> parseCsvList(String value) {
        if (value == null || value.isBlank()) return List.of();
        return List.of(value.split(","));
    }

    private void addCatalog(AcademicoConfigDTO dto, CatalogoAcademico item) {
        if ("ASISTENCIA".equals(item.getTipo()) && "MIN_PORCENTAJE".equals(item.getCodigo())) {
            try {
                dto.setMinAsistenciaPorcentaje(Integer.parseInt(item.getNombre()));
            } catch (NumberFormatException e) {
                dto.setMinAsistenciaPorcentaje(70);
            }
            return;
        }
        if ("INGRESANTES".equals(item.getTipo()) && "MODEL".equals(item.getCodigo())) {
            dto.setIngresantesModelo(item.getNombre());
            return;
        }
        AcademicoConfigDTO.CatalogItemDTO catalogItem = AcademicoConfigDTO.CatalogItemDTO.builder()
                .id(item.getCodigo())
                .label(item.getNombre())
                .active(item.getActivo())
                .build();
        String key = item.getTipo() + "_" + item.getNivel();
        switch (key) {
            // "AREA_CURRICULAR_PRIMARIA": nombre correcto para primaria, usado
            // tanto por el DataInitializer (carga inicial) como por guardar()
            // (ediciones desde el panel de administración) de aqui en adelante.
            case "AREA_CURRICULAR_PRIMARIA" -> dto.getCursosPrimaria().add(catalogItem);
            case "COMPETENCIA_PRIMARIA" -> dto.getCompetenciasPrimaria().add(catalogItem);
            case "CURSO_SECUNDARIA" -> dto.getCursosSecundaria().add(catalogItem);
            case "GRADO_PRIMARIA" -> dto.getGradosPrimaria().add(catalogItem);
            case "GRADO_SECUNDARIA" -> dto.getGradosSecundaria().add(catalogItem);
            case "SECCION_PRIMARIA" -> dto.getSeccionesPrimaria().add(catalogItem);
            case "SECCION_SECUNDARIA" -> dto.getSeccionesSecundaria().add(catalogItem);
            default -> {
            }
        }
    }

    private AcademicoConfigDTO.SalonItemDTO toSalonDto(SalonAcademico salon) {
        return AcademicoConfigDTO.SalonItemDTO.builder()
                .nivel(salon.getNivel())
                .grado(salon.getGrado())
                .seccion(salon.getSeccion())
                .aula(salon.getAula())
                .active(salon.getActivo())
                .build();
    }
}