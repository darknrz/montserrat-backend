package com.monserrat.controller;

import com.monserrat.dto.academico.*;
import com.monserrat.dto.auth.ChangePasswordRequest;
import com.monserrat.service.AcademicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/academico")
@RequiredArgsConstructor
public class AcademicoController {

    private final AcademicoService academicoService;
    private final com.monserrat.service.AcademicoConfigService academicoConfigService;

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioAcademicoDTO> listarUsuarios() {
        return academicoService.listarUsuarios();
    }

    @GetMapping("/configuracion")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALUMNO', 'DOCENTE')")
    public AcademicoConfigDTO obtenerConfiguracion() {
        return academicoConfigService.obtener();
    }

    @PutMapping("/configuracion")
    @PreAuthorize("hasRole('ADMIN')")
    public AcademicoConfigDTO guardarConfiguracion(@RequestBody AcademicoConfigDTO request) {
        return academicoConfigService.guardar(request);
    }

    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioAcademicoDTO> crearUsuario(@Valid @RequestBody CreateUsuarioAcademicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicoService.crearUsuario(request));
    }

    @PutMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioAcademicoDTO actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UpdatePerfilAcademicoRequest request) {
        return academicoService.actualizarUsuario(id, request);
    }

    @DeleteMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean force) {
        academicoService.desactivarUsuario(id, force);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('DOCENTE','ALUMNO')")
    public PerfilAcademicoDTO perfil(Authentication authentication) {
        return academicoService.obtenerPerfil(authentication.getName());
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('DOCENTE','ALUMNO')")
    public PerfilAcademicoDTO actualizarPerfil(Authentication authentication, @Valid @RequestBody UpdatePerfilAcademicoRequest request) {
        return academicoService.actualizarPerfil(authentication.getName(), request);
    }

    @PostMapping("/me/password")
    @PreAuthorize("hasAnyRole('DOCENTE','ALUMNO')")
    public ResponseEntity<Void> cambiarContrasena(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        academicoService.cambiarContrasena(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alumnos")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioAcademicoDTO> listarAlumnos() {
        return academicoService.listarAlumnos();
    }

    @GetMapping("/pensiones")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PensionMensualDTO> listarPensiones(@RequestParam(required = false) Integer anio) {
        return academicoService.listarPensionesMensuales(anio);
    }

    @PutMapping("/pensiones")
    @PreAuthorize("hasRole('ADMIN')")
    public PensionMensualDTO actualizarPension(@Valid @RequestBody PensionMensualRequest request) {
        return academicoService.actualizarPensionMensual(request);
    }

    @GetMapping("/docente/alumnos")
    @PreAuthorize("hasRole('DOCENTE')")
    public List<UsuarioAcademicoDTO> listarAlumnosDocente(Authentication authentication) {
        return academicoService.listarAlumnosDocente(authentication.getName());
    }

    @GetMapping("/docente/asistencias")
    @PreAuthorize("hasRole('DOCENTE')")
    public List<AsistenciaAcademicaDTO> listarAsistenciasDocente(Authentication authentication) {
        return academicoService.listarAsistenciasDocente(authentication.getName());
    }

    @PostMapping("/docente/asistencias")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<AsistenciaAcademicaDTO> registrarAsistencia(Authentication authentication, @Valid @RequestBody AsistenciaAcademicaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicoService.registrarAsistencia(authentication.getName(), request));
    }

    @GetMapping("/docente/notas")
    @PreAuthorize("hasRole('DOCENTE')")
    public List<NotaAcademicaDTO> listarNotasDocente(Authentication authentication) {
        return academicoService.listarNotasDocente(authentication.getName());
    }

    @PostMapping("/docente/notas")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<NotaAcademicaDTO> registrarNota(Authentication authentication, @Valid @RequestBody NotaAcademicaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicoService.registrarNota(authentication.getName(), request));
    }

    @PutMapping("/docente/notas/{id}")
    @PreAuthorize("hasRole('DOCENTE')")
    public NotaAcademicaDTO actualizarNota(Authentication authentication, @PathVariable Long id, @Valid @RequestBody NotaAcademicaRequest request) {
        return academicoService.actualizarNota(authentication.getName(), id, request);
    }

    @GetMapping("/alumno/notas")
    @PreAuthorize("hasRole('ALUMNO')")
    public List<NotaAcademicaDTO> listarNotasAlumno(Authentication authentication) {
        return academicoService.listarNotasAlumno(authentication.getName());
    }

    @GetMapping("/alumno/pension")
    @PreAuthorize("hasRole('ALUMNO')")
    public PensionEstadoDTO obtenerPension(Authentication authentication) {
        return academicoService.obtenerPension(authentication.getName());
    }

    @GetMapping("/alumno/asistencias")
    @PreAuthorize("hasRole('ALUMNO')")
    public List<AsistenciaAcademicaDTO> listarAsistenciasAlumno(Authentication authentication) {
        return academicoService.listarAsistenciasAlumno(authentication.getName());
    }

    @GetMapping("/alumno/pension/detalle")
    @PreAuthorize("hasRole('ALUMNO')")
    public List<PensionMensualDTO> listarPensionesAlumno(Authentication authentication, @RequestParam(required = false) Integer anio) {
        return academicoService.listarPensionesAlumno(authentication.getName(), anio);
    }

    @GetMapping("/asignaciones")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AsignacionAcademicaDTO> listarAsignaciones() {
        return academicoService.listarAsignaciones();
    }

    @PostMapping("/asignaciones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AsignacionAcademicaDTO> crearAsignacion(@Valid @RequestBody AsignacionAcademicaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicoService.crearAsignacion(request));
    }

    @PostMapping("/asignaciones/aula")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AsignacionAcademicaDTO>> asignarAula(@Valid @RequestBody AsignacionAulaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicoService.asignarAula(request));
    }

    @PutMapping("/asignaciones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AsignacionAcademicaDTO actualizarAsignacion(@PathVariable Long id, @Valid @RequestBody AsignacionAcademicaRequest request) {
        return academicoService.actualizarAsignacion(id, request);
    }

    @DeleteMapping("/asignaciones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarAsignacion(@PathVariable Long id) {
        academicoService.eliminarAsignacion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/docente/asignaciones")
    @PreAuthorize("hasRole('DOCENTE')")
    public List<AsignacionAcademicaDTO> listarAsignacionesDocente(Authentication authentication) {
        return academicoService.listarAsignacionesDocente(authentication.getName());
    }

    @GetMapping("/alumno/asignaciones")
    @PreAuthorize("hasRole('ALUMNO')")
    public List<AsignacionAcademicaDTO> listarAsignacionesAlumno(Authentication authentication) {
        return academicoService.listarAsignacionesAlumno(authentication.getName());
    }

    @PostMapping("/importar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImportacionResultDTO> importarUsuarios(@RequestParam("file") MultipartFile file, @RequestParam String tipo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicoService.importarUsuariosDesdeArchivo(file, tipo));
    }
}
