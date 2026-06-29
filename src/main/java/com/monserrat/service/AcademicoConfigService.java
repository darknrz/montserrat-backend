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
import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicoConfigService {

    private final CatalogoAcademicoRepository catalogoRepository;
    private final SalonAcademicoRepository salonRepository;

    @Transactional(readOnly = true)
    public AcademicoConfigDTO obtener() {
        AcademicoConfigDTO dto = new AcademicoConfigDTO();
        catalogoRepository.findAllByOrderByOrdenAscIdAsc().forEach(item -> addCatalog(dto, item));
        dto.setSalones(salonRepository.findAllByOrderByOrdenAscIdAsc().stream().map(this::toSalonDto).toList());
        return dto;
    }

    @Transactional
    public AcademicoConfigDTO guardar(AcademicoConfigDTO request) {
        catalogoRepository.deleteAllInBatch();
        salonRepository.deleteAllInBatch();

        List<CatalogoAcademico> catalogos = new ArrayList<>();
        addCatalogos(catalogos, "CURSO", "PRIMARIA", request.getCursosPrimaria());
        addCatalogos(catalogos, "CURSO", "SECUNDARIA", request.getCursosSecundaria());
        addCatalogos(catalogos, "GRADO", "PRIMARIA", request.getGradosPrimaria());
        addCatalogos(catalogos, "GRADO", "SECUNDARIA", request.getGradosSecundaria());
        addCatalogos(catalogos, "SECCION", "PRIMARIA", request.getSeccionesPrimaria());
        addCatalogos(catalogos, "SECCION", "SECUNDARIA", request.getSeccionesSecundaria());
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
        for (int i = 0; i < safeItems.size(); i++) {
            AcademicoConfigDTO.CatalogItemDTO item = safeItems.get(i);
            target.add(CatalogoAcademico.builder()
                    .tipo(tipo)
                    .nivel(nivel)
                    .codigo(item.getId())
                    .nombre(item.getLabel())
                    .activo(item.getActive() == null || item.getActive())
                    .orden(i)
                    .build());
        }
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
            case "CURSO_PRIMARIA" -> dto.getCursosPrimaria().add(catalogItem);
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
