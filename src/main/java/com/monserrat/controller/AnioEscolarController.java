package com.monserrat.controller;

import com.monserrat.dto.anio.AnioEscolarDTO;
import com.monserrat.dto.anio.MigracionPreviewDTO;
import com.monserrat.dto.anio.MigracionRequest;
import com.monserrat.dto.anio.MigracionResultadoDTO;
import com.monserrat.entity.AsistenciaHistorica;
import com.monserrat.entity.HistorialAlumnoAnio;
import com.monserrat.entity.NotaHistorica;
import com.monserrat.repository.AsistenciaHistoricaRepository;
import com.monserrat.repository.HistorialAlumnoAnioRepository;
import com.monserrat.repository.NotaHistoricaRepository;
import com.monserrat.service.AnioEscolarService;
import com.monserrat.service.MigracionAnioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/academico/anios-escolares")
@RequiredArgsConstructor
public class AnioEscolarController {

    private final AnioEscolarService anioEscolarService;
    private final MigracionAnioService migracionService;
    private final HistorialAlumnoAnioRepository historialRepository;
    private final NotaHistoricaRepository notaHistoricaRepository;
    private final AsistenciaHistoricaRepository asistenciaHistoricaRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_PENSIONES', 'DOCENTE', 'ALUMNO')")
    public List<AnioEscolarDTO> listar() {
        return anioEscolarService.listar();
    }

    @GetMapping("/activo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_PENSIONES', 'DOCENTE', 'ALUMNO')")
    public Map<String, Integer> activo() {
        return Map.of("anio", anioEscolarService.anioActivo());
    }

    /** Simula la migración sin escribir nada. */
    @PostMapping("/migracion/vista-previa")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public MigracionPreviewDTO vistaPrevia(@RequestBody MigracionRequest request) {
        return migracionService.vistaPrevia(request);
    }

    @PostMapping("/migracion/ejecutar")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public MigracionResultadoDTO ejecutar(@RequestBody MigracionRequest request, Authentication authentication) {
        return migracionService.ejecutar(request, authentication.getName());
    }

    // ── Consulta del histórico (solo lectura) ──

    @GetMapping("/{anio}/historial")
    @PreAuthorize("hasRole('ADMIN')")
    public List<HistorialAlumnoAnio> historial(@PathVariable Integer anio) {
        return historialRepository.findByAnioOrderByAlumnoNombreAsc(anio);
    }

    @GetMapping("/{anio}/alumnos/{dni}/notas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<NotaHistorica> notasHistoricas(@PathVariable Integer anio, @PathVariable String dni) {
        return notaHistoricaRepository.findByAnioAndAlumnoDniOrderByPeriodoAscCursoAsc(anio, dni);
    }

    @GetMapping("/{anio}/alumnos/{dni}/asistencias")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AsistenciaHistorica> asistenciasHistoricas(@PathVariable Integer anio, @PathVariable String dni) {
        return asistenciaHistoricaRepository.findByAnioAndAlumnoDniOrderByFechaDesc(anio, dni);
    }
}
