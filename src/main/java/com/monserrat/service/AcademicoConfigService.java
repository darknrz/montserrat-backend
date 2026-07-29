package com.monserrat.service;

import com.monserrat.dto.academico.AcademicoConfigDTO;
import com.monserrat.entity.CatalogoAcademico;
import com.monserrat.entity.SalonAcademico;
import com.monserrat.repository.CatalogoAcademicoRepository;
import com.monserrat.repository.SalonAcademicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AcademicoConfigService {

    private static final int MAX_CATALOGO_NOMBRE_LENGTH = 500;
    private final Object guardarLock = new Object();

    private final CatalogoAcademicoRepository catalogoRepository;
    private final SalonAcademicoRepository salonRepository;
    private final TransactionTemplate transactionTemplate;

    // Constructor explícito (ya no @RequiredArgsConstructor) porque
    // necesitamos construir el TransactionTemplate a partir del
    // PlatformTransactionManager inyectado.
    public AcademicoConfigService(CatalogoAcademicoRepository catalogoRepository,
                                   SalonAcademicoRepository salonRepository,
                                   PlatformTransactionManager transactionManager) {
        this.catalogoRepository = catalogoRepository;
        this.salonRepository = salonRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional(readOnly = true)
    public AcademicoConfigDTO obtener() {
        AcademicoConfigDTO dto = new AcademicoConfigDTO();
        Map<String, List<String>> competenciasPorCurso = new LinkedHashMap<>();
        Map<String, List<String>> docentesPorCompetencia = new LinkedHashMap<>();
        Map<String, List<String>> competenciasPorCursoSecundaria = new LinkedHashMap<>();
        Map<String, List<String>> docentesPorCompetenciaSecundaria = new LinkedHashMap<>();

        catalogoRepository.findAllByOrderByOrdenAscIdAsc().forEach(item -> {
            addCatalog(dto, item);
            if ("COMPETENCIA_CURSO".equals(item.getTipo()) && "PRIMARIA".equals(item.getNivel())) {
                competenciasPorCurso.put(item.getCodigo(), parseCsvList(item.getNombre()));
            }
            if ("DOCENTE_COMPETENCIA".equals(item.getTipo()) && "PRIMARIA".equals(item.getNivel())) {
                docentesPorCompetencia.put(item.getCodigo(), parseCsvList(item.getNombre()));
            }
            if ("COMPETENCIA_CURSO".equals(item.getTipo()) && "SECUNDARIA".equals(item.getNivel())) {
                competenciasPorCursoSecundaria.put(item.getCodigo(), parseCsvList(item.getNombre()));
            }
            if ("DOCENTE_COMPETENCIA".equals(item.getTipo()) && "SECUNDARIA".equals(item.getNivel())) {
                docentesPorCompetenciaSecundaria.put(item.getCodigo(), parseCsvList(item.getNombre()));
            }
        });

        dto.setCompetenciasPorCursoPrimaria(competenciasPorCurso);
        dto.setDocentesPorCompetencia(docentesPorCompetencia);
        dto.setCompetenciasPorCursoSecundaria(competenciasPorCursoSecundaria);
        dto.setDocentesPorCompetenciaSecundaria(docentesPorCompetenciaSecundaria);
        dto.setSalones(salonRepository.findAllByOrderByOrdenAscIdAsc().stream().map(this::toSalonDto).toList());
        return dto;
    }

    // Ya NO lleva @Transactional a nivel de método: la transacción se maneja
    // explícitamente con transactionTemplate DENTRO del synchronized, para
    // que el lock no se libere hasta que el commit ya se haya hecho.
    public AcademicoConfigDTO guardar(AcademicoConfigDTO request) {
        synchronized (guardarLock) {
            transactionTemplate.executeWithoutResult(status -> {
                catalogoRepository.deleteAllInBatch();
                salonRepository.deleteAllInBatch();
                catalogoRepository.flush();
                salonRepository.flush();

                List<CatalogoAcademico> catalogos = new ArrayList<>();
                // Primaria usa "AREA_CURRICULAR" (término correcto del currículo peruano
                // para ese nivel); secundaria usa "CURSO". Mantener esta distinción
                // consistente en TODO el flujo: DataInitializer, guardar() y el switch
                // de addCatalog() de abajo deben usar siempre las mismas dos etiquetas.
                addCatalogos(catalogos, "AREA_CURRICULAR", "PRIMARIA", request.getCursosPrimaria());
                addCatalogos(catalogos, "CURSO", "SECUNDARIA", request.getCursosSecundaria());
                addCatalogos(catalogos, "GRADO", "PRIMARIA", request.getGradosPrimaria());
                addCatalogos(catalogos, "COMPETENCIA", "PRIMARIA", request.getCompetenciasPrimaria());
                addCatalogos(catalogos, "COMPETENCIA", "SECUNDARIA", request.getCompetenciasSecundaria());
                addCatalogos(catalogos, "GRADO", "SECUNDARIA", request.getGradosSecundaria());
                addCatalogos(catalogos, "SECCION", "PRIMARIA", request.getSeccionesPrimaria());
                addCatalogos(catalogos, "SECCION", "SECUNDARIA", request.getSeccionesSecundaria());
                addCatalogos(catalogos, "NIVEL_ACADEMICO", "GLOBAL", request.getNivelesAcademicos());
                addCompetenciasPorCurso(catalogos, request.getCompetenciasPorCursoPrimaria());
                addDocentesPorCompetencia(catalogos, request.getDocentesPorCompetencia());
                addCompetenciasPorCursoSecundaria(catalogos, request.getCompetenciasPorCursoSecundaria());
                addDocentesPorCompetenciaSecundaria(catalogos, request.getDocentesPorCompetenciaSecundaria());
                catalogoRepository.saveAll(dedupeCatalogos(catalogos));

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
            });
            // Para cuando salimos de aquí, el commit YA ocurrió (TransactionTemplate
            // hace commit al terminar executeWithoutResult), así que es seguro
            // liberar el lock y leer el estado recién guardado.
            return obtener();
        }
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
            String rawId = item.getId() == null ? "" : item.getId().trim();
            if (rawId.isBlank()) {
                continue;
            }
            String id = normalizeCodigo(rawId);
            uniqueItems.putIfAbsent(id, item);
        }

        int index = 0;
        for (Map.Entry<String, AcademicoConfigDTO.CatalogItemDTO> entry : uniqueItems.entrySet()) {
            CatalogoAcademico catalogo = CatalogoAcademico.builder()
                    .tipo(tipo)
                    .nivel(nivel)
                    .codigo(entry.getKey())
                    .nombre(entry.getValue().getLabel())
                    .activo(entry.getValue().getActive() == null || entry.getValue().getActive())
                    .orden(index++)
                    .build();
            target.add(catalogo);
        }
    }

    private List<CatalogoAcademico> dedupeCatalogos(List<CatalogoAcademico> catalogos) {
        Map<String, CatalogoAcademico> unique = new LinkedHashMap<>();
        for (CatalogoAcademico catalogo : catalogos) {
            String tipo = normalizeTipoNivel(catalogo.getTipo());
            String nivel = normalizeTipoNivel(catalogo.getNivel());
            String codigo = normalizeCodigo(catalogo.getCodigo());
            catalogo.setTipo(tipo);
            catalogo.setNivel(nivel);
            catalogo.setCodigo(codigo);
            String key = String.join("|", tipo, nivel, codigo);
            unique.putIfAbsent(key, catalogo);
        }
        return new ArrayList<>(unique.values());
    }

    private String normalizeTipoNivel(String value) {
        if (value == null) return "";
        return value.trim().toUpperCase();
    }

    private String normalizeCodigo(String codigo) {
        if (codigo == null) return "";
        return codigo.trim().toUpperCase();
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

    private void addDocentesPorCompetencia(List<CatalogoAcademico> target, Map<String, List<String>> mappings) {
        if (mappings == null || mappings.isEmpty()) return;
        int index = 2000;
        for (Map.Entry<String, List<String>> entry : mappings.entrySet()) {
            target.add(CatalogoAcademico.builder()
                    .tipo("DOCENTE_COMPETENCIA")
                    .nivel("PRIMARIA")
                    .codigo(entry.getKey())
                    .nombre(serializeList(entry.getValue()))
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
            case "NIVEL_ACADEMICO_GLOBAL" -> dto.getNivelesAcademicos().add(catalogItem);
            case "COMPETENCIA_SECUNDARIA" -> dto.getCompetenciasSecundaria().add(catalogItem);
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

    private void addCompetenciasPorCursoSecundaria(List<CatalogoAcademico> target, Map<String, List<String>> mappings) {
        if (mappings == null || mappings.isEmpty()) return;
        int index = 3000;
        for (Map.Entry<String, List<String>> entry : mappings.entrySet()) {
            target.add(CatalogoAcademico.builder()
                    .tipo("COMPETENCIA_CURSO")
                    .nivel("SECUNDARIA")
                    .codigo(entry.getKey())
                    .nombre(serializeList(entry.getValue()))
                    .activo(true)
                    .orden(index++)
                    .build());
        }
    }

    private void addDocentesPorCompetenciaSecundaria(List<CatalogoAcademico> target, Map<String, List<String>> mappings) {
        if (mappings == null || mappings.isEmpty()) return;
        int index = 4000;
        for (Map.Entry<String, List<String>> entry : mappings.entrySet()) {
            target.add(CatalogoAcademico.builder()
                    .tipo("DOCENTE_COMPETENCIA")
                    .nivel("SECUNDARIA")
                    .codigo(entry.getKey())
                    .nombre(serializeList(entry.getValue()))
                    .activo(true)
                    .orden(index++)
                    .build());
        }
    }
}