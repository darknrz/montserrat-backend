package com.monserrat.service;

import com.monserrat.dto.academico.AcademicoConfigDTO;
import com.monserrat.entity.CatalogoAcademico;
import com.monserrat.entity.SalonAcademico;
import com.monserrat.repository.CatalogoAcademicoRepository;
import com.monserrat.repository.SalonAcademicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicoConfigServiceTest {

    @Mock
    private CatalogoAcademicoRepository catalogoRepository;

    @Mock
    private SalonAcademicoRepository salonRepository;

    @InjectMocks
    private AcademicoConfigService academicoConfigService;

    @Test
    void guardarDebePersistirMapasDeCompetenciasYDocentes() {
        List<CatalogoAcademico> savedCatalogos = new ArrayList<>();

        when(catalogoRepository.save(any(CatalogoAcademico.class))).thenAnswer(invocation -> {
            CatalogoAcademico catalogo = invocation.getArgument(0);
            catalogo.setId((long) (savedCatalogos.size() + 1));
            savedCatalogos.add(catalogo);
            return catalogo;
        });
        when(catalogoRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<CatalogoAcademico> catalogos = invocation.getArgument(0);
            for (CatalogoAcademico catalogo : catalogos) {
                catalogo.setId((long) (savedCatalogos.size() + 1));
                savedCatalogos.add(catalogo);
            }
            return catalogos;
        });
        when(catalogoRepository.findAllByOrderByOrdenAscIdAsc()).thenAnswer(invocation -> new ArrayList<>(savedCatalogos));
        when(salonRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(salonRepository.findAllByOrderByOrdenAscIdAsc()).thenReturn(List.of());

        AcademicoConfigDTO request = AcademicoConfigDTO.builder()
            .cursosPrimaria(List.of())
            .competenciasPrimaria(List.of())
            .cursosSecundaria(List.of())
            .gradosPrimaria(List.of())
            .gradosSecundaria(List.of())
            .seccionesPrimaria(List.of())
            .seccionesSecundaria(List.of())
            .salones(List.of())
            .competenciasPorCursoPrimaria(Map.of("MATEMATICA", List.of("C1", "C2")))
            .docentesPorCompetencia(Map.of("PRIMERO_PRIMARIA||MATEMATICA||C1", List.of("12345678", "87654321")))
            .build();

        AcademicoConfigDTO result = academicoConfigService.guardar(request);

        assertEquals(List.of("C1", "C2"), result.getCompetenciasPorCursoPrimaria().get("MATEMATICA"));
        assertEquals(List.of("12345678", "87654321"), result.getDocentesPorCompetencia().get("PRIMERO_PRIMARIA||MATEMATICA||C1"));
    }

    @Test
    void guardarDebeIgnorarCompetenciasDuplicadasEnLaSolicitud() {
        List<CatalogoAcademico> savedCatalogos = new ArrayList<>();

        when(catalogoRepository.save(any(CatalogoAcademico.class))).thenAnswer(invocation -> {
            CatalogoAcademico catalogo = invocation.getArgument(0);
            catalogo.setId((long) (savedCatalogos.size() + 1));
            savedCatalogos.add(catalogo);
            return catalogo;
        });
        when(catalogoRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<CatalogoAcademico> catalogos = invocation.getArgument(0);
            for (CatalogoAcademico catalogo : catalogos) {
                catalogo.setId((long) (savedCatalogos.size() + 1));
                savedCatalogos.add(catalogo);
            }
            return catalogos;
        });
        when(catalogoRepository.findAllByOrderByOrdenAscIdAsc()).thenAnswer(invocation -> new ArrayList<>(savedCatalogos));
        when(salonRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(salonRepository.findAllByOrderByOrdenAscIdAsc()).thenReturn(List.of());

        AcademicoConfigDTO request = AcademicoConfigDTO.builder()
            .cursosPrimaria(List.of())
            .competenciasPrimaria(List.of(
                AcademicoConfigDTO.CatalogItemDTO.builder().id("C1").label("Primera").active(true).build(),
                AcademicoConfigDTO.CatalogItemDTO.builder().id("C1").label("Primera duplicada").active(true).build(),
                AcademicoConfigDTO.CatalogItemDTO.builder().id("C2").label("Segunda").active(true).build()
            ))
            .cursosSecundaria(List.of())
            .gradosPrimaria(List.of())
            .gradosSecundaria(List.of())
            .seccionesPrimaria(List.of())
            .seccionesSecundaria(List.of())
            .salones(List.of())
            .build();

        AcademicoConfigDTO result = academicoConfigService.guardar(request);
        System.out.println("DEBUG competencias: " + result.getCompetenciasPrimaria());

        long competenciasPersistidas = result.getCompetenciasPrimaria().stream()
            .filter(item -> "C1".equals(item.getId()) || "C2".equals(item.getId()))
            .count();

        assertEquals(2L, competenciasPersistidas);
        assertTrue(result.getCompetenciasPrimaria().stream().anyMatch(item -> "C1".equals(item.getId()) && "Primera".equals(item.getLabel())));
    }
}
