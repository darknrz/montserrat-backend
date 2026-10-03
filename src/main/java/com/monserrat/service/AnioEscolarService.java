package com.monserrat.service;

import com.monserrat.dto.anio.AnioEscolarDTO;
import com.monserrat.entity.AnioEscolar;
import com.monserrat.entity.EstadoAnioEscolar;
import com.monserrat.repository.AnioEscolarRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;

/**
 * Fuente única del "año escolar activo". Reemplaza el uso disperso de {@code Year.now()}: los listados
 * que no reciben un año explícito usan el año activo, que el admin cambia solo con la migración.
 */
@Service
@RequiredArgsConstructor
public class AnioEscolarService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnioEscolarService.class);

    private final AnioEscolarRepository repository;
    private final JdbcTemplate jdbcTemplate;

    private volatile Integer cacheAnioActivo;

    @EventListener(ApplicationReadyEvent.class)
    public void inicializar() {
        crearIndiceUnicoActivo();
        if (repository.findFirstByEstado(EstadoAnioEscolar.ACTIVO).isEmpty()) {
            int anio = Year.now().getValue();
            AnioEscolar creado = repository.findByAnio(anio)
                    .map(existente -> {
                        existente.setEstado(EstadoAnioEscolar.ACTIVO);
                        return existente;
                    })
                    .orElseGet(() -> AnioEscolar.builder().anio(anio).estado(EstadoAnioEscolar.ACTIVO).build());
            repository.save(creado);
            LOGGER.info("Año escolar activo inicializado: {}", anio);
        }
        cacheAnioActivo = null;
    }

    /** Año escolar activo; si aún no hay ninguno registrado cae al año calendario. */
    public int anioActivo() {
        Integer cached = cacheAnioActivo;
        if (cached != null) {
            return cached;
        }
        int anio = repository.findFirstByEstado(EstadoAnioEscolar.ACTIVO)
                .map(AnioEscolar::getAnio)
                .orElse(Year.now().getValue());
        cacheAnioActivo = anio;
        return anio;
    }

    public void invalidarCache() {
        cacheAnioActivo = null;
    }

    public List<AnioEscolarDTO> listar() {
        return repository.findAllByOrderByAnioDesc().stream().map(AnioEscolarService::toDto).toList();
    }

    static AnioEscolarDTO toDto(AnioEscolar a) {
        return AnioEscolarDTO.builder()
                .id(a.getId())
                .anio(a.getAnio())
                .estado(a.getEstado())
                .fechaCierre(a.getFechaCierre())
                .cerradoPor(a.getCerradoPor())
                .totalPromovidos(a.getTotalPromovidos())
                .totalRepitentes(a.getTotalRepitentes())
                .totalEgresados(a.getTotalEgresados())
                .totalRetirados(a.getTotalRetirados())
                .build();
    }

    /** Índice parcial: como máximo una fila con estado ACTIVO. */
    private void crearIndiceUnicoActivo() {
        try {
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_anio_escolar_unico_activo "
                    + "ON anios_escolares ((1)) WHERE estado = 'ACTIVO'");
        } catch (Exception ex) {
            LOGGER.warn("No se pudo crear el índice de año activo único: {}", ex.getMessage());
        }
    }
}
