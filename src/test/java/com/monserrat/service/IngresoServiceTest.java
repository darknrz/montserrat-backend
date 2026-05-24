package com.monserrat.service;

import com.monserrat.dto.IngresoDTO;
import com.monserrat.entity.Ingreso;
import com.monserrat.repository.IngresoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngresoServiceTest {

    @Mock
    private IngresoRepository ingresoRepository;

    @InjectMocks
    private IngresoService ingresoService;

    @Test
    void getAllReturnsOnlyActiveIngresantesAsDto() {
        Ingreso ingreso = ingreso(1L, true);
        when(ingresoRepository.findByActivoTrue()).thenReturn(List.of(ingreso));

        List<IngresoDTO> result = ingresoService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Ana Torres");
        assertThat(result.get(0).getUniversidadSiglas()).isEqualTo("uncp");
    }

    @Test
    void createUppercasesUniversitySiglasAndDefaultsActivoToTrue() {
        IngresoDTO dto = IngresoDTO.builder()
                .nombre("Ana Torres")
                .universidad("Universidad Nacional del Centro del Peru")
                .universidadSiglas("uncp")
                .carrera("Ingenieria de Sistemas")
                .anio("2026")
                .tipoSeleccion("1ra Seleccion")
                .fotoUrl("https://example.test/foto.jpg")
                .build();

        when(ingresoRepository.save(any(Ingreso.class))).thenAnswer(invocation -> {
            Ingreso saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        IngresoDTO result = ingresoService.create(dto);

        ArgumentCaptor<Ingreso> captor = ArgumentCaptor.forClass(Ingreso.class);
        verify(ingresoRepository).save(captor.capture());
        assertThat(captor.getValue().getUniversidadSiglas()).isEqualTo("UNCP");
        assertThat(captor.getValue().getActivo()).isTrue();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUniversidadSiglas()).isEqualTo("UNCP");
    }

    @Test
    void deleteMarksIngresanteAsInactive() {
        Ingreso ingreso = ingreso(5L, true);
        when(ingresoRepository.findById(5L)).thenReturn(Optional.of(ingreso));

        ingresoService.delete(5L);

        assertThat(ingreso.getActivo()).isFalse();
        verify(ingresoRepository).save(ingreso);
    }

    @Test
    void getByIdThrowsWhenIngresanteDoesNotExist() {
        when(ingresoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingresoService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Ingresante no encontrado: 99");
    }

    private static Ingreso ingreso(Long id, Boolean activo) {
        return Ingreso.builder()
                .id(id)
                .nombre("Ana Torres")
                .universidad("Universidad Nacional del Centro del Peru")
                .universidadSiglas("uncp")
                .carrera("Ingenieria de Sistemas")
                .anio("2026")
                .tipoSeleccion("1ra Seleccion")
                .fotoUrl("https://example.test/foto.jpg")
                .activo(activo)
                .build();
    }
}
