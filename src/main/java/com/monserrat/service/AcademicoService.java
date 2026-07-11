package com.monserrat.service;

import com.monserrat.dto.academico.*;
import com.monserrat.dto.auth.ChangePasswordRequest;
import com.monserrat.entity.AsignacionAcademica;
import com.monserrat.entity.AsistenciaAcademica;
import com.monserrat.entity.CursoAcademico;
import com.monserrat.entity.EstadoMatricula;
import com.monserrat.entity.EstadoUsuario;
import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.NotaAcademica;
import com.monserrat.entity.PensionMensual;
import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.repository.AsignacionAcademicaRepository;
import com.monserrat.repository.AsistenciaAcademicaRepository;
import com.monserrat.repository.NotaAcademicaRepository;
import com.monserrat.repository.PensionMensualRepository;
import com.monserrat.repository.UsuarioAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicoService {

    private final UsuarioAcademicoRepository usuarioRepository;
    private final AsignacionAcademicaRepository asignacionRepository;
    private final AsistenciaAcademicaRepository asistenciaRepository;
    private final NotaAcademicaRepository notaRepository;
    private final PensionMensualRepository pensionMensualRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioAcademicoDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream().map(this::toUsuarioDto).toList();
    }

    public List<UsuarioAcademicoDTO> listarAlumnos() {
        return usuarioRepository.findByRolAndActivoTrue(RolUsuario.ALUMNO).stream().map(this::toUsuarioDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioAcademicoDTO> listarAlumnosDocente(String docenteDni) {
        return asignacionRepository.findByDocente_DniAndActivoTrue(docenteDni).stream()
                .map(AsignacionAcademica::getAlumno)
                .filter(UsuarioAcademico::getActivo)
                .collect(Collectors.toMap(
                        UsuarioAcademico::getDni,
                        this::toUsuarioDto,
                        (left, right) -> left,
                        LinkedHashMap::new))
                .values()
                .stream()
                .sorted(Comparator.comparing(UsuarioAcademicoDTO::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public UsuarioAcademicoDTO crearUsuario(CreateUsuarioAcademicoRequest request) {
        RolUsuario rol = request.getRol() == null ? RolUsuario.ALUMNO : request.getRol();
        if (RolUsuario.ADMIN.equals(rol)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El administrador se gestiona desde la tabla de administradores");
        }
        validarDatosAcademicos(rol, request.getNivelEducativo(), request.getGrado());
        validarDatosUnicos(null, request.getDni(), request.getCodigo(), request.getCorreo());

        UsuarioAcademico usuario = UsuarioAcademico.builder()
                .dni(request.getDni())
                .codigo(request.getCodigo())
                .password(passwordEncoder.encode(request.getDni()))
                .nombre(request.getNombre())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .correo(request.getCorreo())
                .direccion(request.getDireccion())
                .fechaNacimiento(request.getFechaNacimiento())
                .rol(rol)
                .telefono(request.getTelefono())
                .fotoUrl(request.getFotoUrl())
                .nivelEducativo(request.getNivelEducativo())
                .grado(request.getGrado())
                .seccion(request.getSeccion())
                .materia(request.getMateria())
                .especialidad(request.getEspecialidad())
                .estado(request.getEstado() == null ? EstadoUsuario.ACTIVO : request.getEstado())
                .estadoMatricula(request.getEstadoMatricula() == null ? EstadoMatricula.MATRICULADO
                        : request.getEstadoMatricula())
                .pensionPagada(Boolean.TRUE.equals(request.getPensionPagada()))
                .pensionObservacion(request.getPensionObservacion())
                .createdAt(request.getCreatedAt())
                .inicioPeriodo(request.getInicioPeriodo())
                .debeCambiarContrasena(true)
                .activo(true)
                .build();

        return toUsuarioDto(usuarioRepository.save(usuario));
    }

    public UsuarioAcademicoDTO actualizarUsuario(Long id, UpdatePerfilAcademicoRequest request) {
        UsuarioAcademico usuario = buscarPorId(id);
        aplicarPerfil(usuario, request, true);
        return toUsuarioDto(usuarioRepository.save(usuario));
    }

    @Transactional
    public void desactivarUsuario(Long id, boolean forceDelete) {
        UsuarioAcademico usuario = buscarPorId(id);
        String dependencies = construirDependenciasEliminacion(usuario);
        if (!dependencies.isBlank()) {
            if (!forceDelete) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, dependencies);
            }
            eliminarConDependencias(usuario);
            return;
        }
        usuarioRepository.delete(usuario);
    }

    private void eliminarConDependencias(UsuarioAcademico usuario) {
        String dni = usuario.getDni();
        if (RolUsuario.DOCENTE.equals(usuario.getRol())) {
            asignacionRepository.deleteByDocente_Dni(dni);
            asistenciaRepository.deleteByDocente_Dni(dni);
            notaRepository.deleteByDocente_Dni(dni);
        }
        if (RolUsuario.ALUMNO.equals(usuario.getRol())) {
            asignacionRepository.deleteByAlumno_Dni(dni);
            asistenciaRepository.deleteByAlumno_Dni(dni);
            notaRepository.deleteByAlumno_Dni(dni);
            pensionMensualRepository.deleteByAlumno_Dni(dni);
        }
        usuarioRepository.delete(usuario);
    }

    public PerfilAcademicoDTO obtenerPerfil(String dni) {
        return toPerfilDto(buscarPorDni(dni));
    }

    public PerfilAcademicoDTO actualizarPerfil(String dni, UpdatePerfilAcademicoRequest request) {
        UsuarioAcademico usuario = buscarPorDni(dni);
        aplicarPerfil(usuario, request, false);
        return toPerfilDto(usuarioRepository.save(usuario));
    }

    public void cambiarContrasena(String dni, ChangePasswordRequest request) {
        UsuarioAcademico usuario = buscarPorDni(dni);
        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasena actual no es correcta");
        }
        if (passwordEncoder.matches(request.getNewPassword(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contrasena debe ser diferente");
        }
        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usuario.setDebeCambiarContrasena(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public AsistenciaAcademicaDTO registrarAsistencia(String docenteDni, AsistenciaAcademicaRequest request) {
        UsuarioAcademico docente = exigirRol(buscarPorDni(docenteDni), RolUsuario.DOCENTE);
        UsuarioAcademico alumno = exigirRol(buscarPorDni(request.getAlumnoDni()), RolUsuario.ALUMNO);
        exigirAsignacionActiva(docente.getDni(), alumno.getDni(), null);

        AsistenciaAcademica asistencia = AsistenciaAcademica.builder()
                .docente(docente)
                .alumno(alumno)
                .fecha(request.getFecha())
                .estado(request.getEstado())
                .observacion(request.getObservacion())
                .build();

        return toAsistenciaDto(asistenciaRepository.save(asistencia));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaAcademicaDTO> listarAsistenciasDocente(String docenteDni) {
        return asistenciaRepository.findByDocente_DniOrderByFechaDesc(docenteDni).stream()
                .map(this::toAsistenciaDto)
                .toList();
    }

    @Transactional
    public NotaAcademicaDTO registrarNota(String docenteDni, NotaAcademicaRequest request) {
        UsuarioAcademico docente = exigirRol(buscarPorDni(docenteDni), RolUsuario.DOCENTE);
        UsuarioAcademico alumno = exigirRol(buscarPorDni(request.getAlumnoDni()), RolUsuario.ALUMNO);
        CursoAcademico curso = request.getCurso();
        exigirAsignacionActiva(docente.getDni(), alumno.getDni(), curso);

        NotaAcademica nota = NotaAcademica.builder()
                .docente(docente)
                .alumno(alumno)
                .curso(curso)
                .periodo(normalizarTexto(request.getPeriodo()))
                .tipoEvaluacion(request.getTipoEvaluacion())
                .valor(request.getValor())
                .observacion(request.getObservacion())
                .competenciaId(request.getCompetenciaId())
                .build();

        return toNotaDto(notaRepository.save(nota));
    }

    @Transactional
    public NotaAcademicaDTO actualizarNota(String docenteDni, Long id, NotaAcademicaRequest request) {
        NotaAcademica nota = notaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nota no encontrada"));
        if (!nota.getDocente().getDni().equals(docenteDni)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar una nota de otro docente");
        }
        UsuarioAcademico alumno = exigirRol(buscarPorDni(request.getAlumnoDni()), RolUsuario.ALUMNO);
        CursoAcademico curso = request.getCurso();
        exigirAsignacionActiva(docenteDni, alumno.getDni(), curso);
        nota.setAlumno(alumno);
        nota.setCurso(curso);
        nota.setPeriodo(normalizarTexto(request.getPeriodo()));
        nota.setTipoEvaluacion(request.getTipoEvaluacion());
        nota.setValor(request.getValor());
        nota.setObservacion(request.getObservacion());
        nota.setCompetenciaId(request.getCompetenciaId());
        return toNotaDto(notaRepository.save(nota));
    }

    @Transactional(readOnly = true)
    public List<NotaAcademicaDTO> listarNotasDocente(String docenteDni) {
        return notaRepository.findByDocente_DniOrderByUpdatedAtDesc(docenteDni).stream()
                .map(this::toNotaDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotaAcademicaDTO> listarNotasAlumno(String alumnoDni) {
        return notaRepository.findByAlumno_DniOrderByPeriodoDescCreatedAtDesc(alumnoDni).stream()
                .map(this::toNotaDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsistenciaAcademicaDTO> listarAsistenciasAlumno(String alumnoDni) {
        return asistenciaRepository.findByAlumno_DniOrderByFechaDesc(alumnoDni).stream()
                .map(this::toAsistenciaDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PensionMensualDTO> listarPensionesAlumno(String alumnoDni, Integer anio) {
        int year = anio == null ? java.time.Year.now().getValue() : anio;
        UsuarioAcademico alumno = exigirRol(buscarPorDni(alumnoDni), RolUsuario.ALUMNO);
        List<PensionMensual> pagos = pensionMensualRepository.findByAlumno_DniAndAnio(alumno.getDni(), year);
        java.util.Map<Integer, PensionMensual> pagosMap = pagos.stream()
                .collect(Collectors.toMap(PensionMensual::getMes, p -> p, (a, b) -> a));
        List<PensionMensualDTO> result = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            boolean activa = mesPensionActiva(alumno, year, mes);
            result.add(toPensionMensualDto(alumno, year, mes, pagosMap.get(mes), activa));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<AsignacionAcademicaDTO> listarAsignaciones() {
        return asignacionRepository.findByActivoTrue().stream()
                .sorted(Comparator
                        .comparing((AsignacionAcademica a) -> a.getCurso().name(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(a -> a.getDocente().getNombre(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(a -> a.getAlumno().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .map(this::toAsignacionDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsignacionAcademicaDTO> listarAsignacionesDocente(String docenteDni) {
        return asignacionRepository.findByDocente_DniAndActivoTrue(docenteDni).stream()
                .sorted(Comparator
                        .comparing((AsignacionAcademica a) -> a.getCurso().name(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(a -> a.getAlumno().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .map(this::toAsignacionDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsignacionAcademicaDTO> listarAsignacionesAlumno(String alumnoDni) {
        return asignacionRepository.findByAlumno_DniAndActivoTrue(alumnoDni).stream()
                .sorted(Comparator
                        .comparing((AsignacionAcademica a) -> a.getCurso().name(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(a -> a.getDocente().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .map(this::toAsignacionDto)
                .toList();
    }

    public AsignacionAcademicaDTO crearAsignacion(AsignacionAcademicaRequest request) {
        UsuarioAcademico docente = exigirRol(buscarPorDni(request.getDocenteDni()), RolUsuario.DOCENTE);
        UsuarioAcademico alumno = exigirRol(buscarPorDni(request.getAlumnoDni()), RolUsuario.ALUMNO);
        validarDatosAcademicos(RolUsuario.ALUMNO, request.getNivelEducativo(), request.getGrado());
        validarAlumnoEnAula(alumno, request.getNivelEducativo(), request.getGrado(), request.getSeccion());
        validarDocenteCurso(docente, request.getNivelEducativo(), request.getCurso());
        if (NivelEducativo.PRIMARIA.equals(request.getNivelEducativo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "En primaria asigna el docente al salon completo desde la opcion de asignacion de aula");
        }
        if (asignacionRepository.existsByDocente_DniAndAlumno_DniAndCursoAndGradoAndSeccionAndActivoTrue(
                docente.getDni(), alumno.getDni(), request.getCurso(), request.getGrado(), request.getSeccion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe esta asignacion");
        }

        AsignacionAcademica asignacion = AsignacionAcademica.builder()
                .docente(docente)
                .alumno(alumno)
                .curso(request.getCurso())
                .nivelEducativo(request.getNivelEducativo())
                .grado(request.getGrado())
                .seccion(request.getSeccion())
                .activo(request.getActivo() == null || request.getActivo())
                .build();
        return toAsignacionDto(asignacionRepository.save(asignacion));
    }

    @Transactional
    public List<AsignacionAcademicaDTO> asignarAula(AsignacionAulaRequest request) {
        UsuarioAcademico docente = exigirRol(buscarPorDni(request.getDocenteDni()), RolUsuario.DOCENTE);
        validarDatosAcademicos(RolUsuario.ALUMNO, request.getNivelEducativo(), request.getGrado());
        validarDocenteCurso(docente, request.getNivelEducativo(), request.getCurso());
        validarDocentePrimariaUnSoloSalon(docente, request);

        List<UsuarioAcademico> alumnos = usuarioRepository.findByRolAndNivelEducativoAndGradoAndSeccionAndActivoTrue(
                RolUsuario.ALUMNO, request.getNivelEducativo(), request.getGrado(), request.getSeccion());
        if (alumnos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay alumnos activos en el aula seleccionada");
        }

        List<CursoAcademico> cursos = cursosParaAsignacionAula(request);
        List<AsignacionAcademica> actuales = asignacionRepository.findByNivelEducativoAndGradoAndSeccionAndActivoTrue(
                request.getNivelEducativo(), request.getGrado(), request.getSeccion());
        List<AsignacionAcademica> cambios = new ArrayList<>();

        for (UsuarioAcademico alumno : alumnos) {
            for (CursoAcademico curso : cursos) {
                actuales.stream()
                        .filter(a -> a.getAlumno().getDni().equals(alumno.getDni()) && a.getCurso() == curso)
                        .filter(a -> !a.getDocente().getDni().equals(docente.getDni()))
                        .forEach(a -> {
                            a.setActivo(false);
                            cambios.add(a);
                        });

                boolean existe = actuales.stream()
                        .anyMatch(a -> a.getAlumno().getDni().equals(alumno.getDni())
                                && a.getDocente().getDni().equals(docente.getDni())
                                && a.getCurso() == curso);
                if (!existe) {
                    cambios.add(AsignacionAcademica.builder()
                            .docente(docente)
                            .alumno(alumno)
                            .curso(curso)
                            .nivelEducativo(request.getNivelEducativo())
                            .grado(request.getGrado())
                            .seccion(request.getSeccion())
                            .activo(request.getActivo() == null || request.getActivo())
                            .build());
                }
            }
        }

        return asignacionRepository.saveAll(cambios).stream()
                .filter(AsignacionAcademica::getActivo)
                .map(this::toAsignacionDto)
                .toList();
    }

    public AsignacionAcademicaDTO actualizarAsignacion(Long id, AsignacionAcademicaRequest request) {
        AsignacionAcademica asignacion = asignacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asignacion no encontrada"));
        UsuarioAcademico docente = exigirRol(buscarPorDni(request.getDocenteDni()), RolUsuario.DOCENTE);
        UsuarioAcademico alumno = exigirRol(buscarPorDni(request.getAlumnoDni()), RolUsuario.ALUMNO);
        validarDatosAcademicos(RolUsuario.ALUMNO, request.getNivelEducativo(), request.getGrado());
        validarAlumnoEnAula(alumno, request.getNivelEducativo(), request.getGrado(), request.getSeccion());
        validarDocenteCurso(docente, request.getNivelEducativo(), request.getCurso());
        if (NivelEducativo.PRIMARIA.equals(request.getNivelEducativo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "En primaria asigna el docente al salon completo desde la opcion de asignacion de aula");
        }

        asignacion.setDocente(docente);
        asignacion.setAlumno(alumno);
        asignacion.setCurso(request.getCurso());
        asignacion.setNivelEducativo(request.getNivelEducativo());
        asignacion.setGrado(request.getGrado());
        asignacion.setSeccion(request.getSeccion());
        if (request.getActivo() != null) {
            asignacion.setActivo(request.getActivo());
        }
        return toAsignacionDto(asignacionRepository.save(asignacion));
    }

    public void eliminarAsignacion(Long id) {
        AsignacionAcademica asignacion = asignacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asignacion no encontrada"));
        asignacion.setActivo(false);
        asignacionRepository.save(asignacion);
    }

    public PensionEstadoDTO obtenerPension(String alumnoDni) {
        UsuarioAcademico alumno = exigirRol(buscarPorDni(alumnoDni), RolUsuario.ALUMNO);
        return PensionEstadoDTO.builder()
                .dni(alumno.getDni())
                .nombre(alumno.getNombre())
                .pagada(alumno.getPensionPagada())
                .observacion(alumno.getPensionObservacion())
                .actualizadoEn(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PensionMensualDTO> listarPensionesMensuales(Integer anio) {
        int year = anio == null ? java.time.Year.now().getValue() : anio;
        List<UsuarioAcademico> alumnos = usuarioRepository.findByRolAndActivoTrue(RolUsuario.ALUMNO).stream()
                .sorted(Comparator
                        .comparing((UsuarioAcademico alumno) -> alumno.getNivelEducativo() == null ? ""
                                : alumno.getNivelEducativo().name())
                        .thenComparing(alumno -> alumno.getGrado() == null ? "" : alumno.getGrado().name())
                        .thenComparing(alumno -> alumno.getSeccion() == null ? "" : alumno.getSeccion().name())
                        .thenComparing(UsuarioAcademico::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
        java.util.Map<String, PensionMensual> pagos = pensionMensualRepository.findByAnio(year).stream()
                .collect(Collectors.toMap(
                        pago -> pago.getAlumno().getDni() + "-" + pago.getMes(),
                        pago -> pago,
                        (left, right) -> left));
        List<PensionMensualDTO> result = new ArrayList<>();
        for (UsuarioAcademico alumno : alumnos) {
            for (int mes = 1; mes <= 12; mes++) {
                boolean activa = mesPensionActiva(alumno, year, mes);
                PensionMensual pago = pagos.get(alumno.getDni() + "-" + mes);
                result.add(toPensionMensualDto(alumno, year, mes, pago, activa));
            }
        }
        return result;
    }

    @Transactional
    public PensionMensualDTO actualizarPensionMensual(PensionMensualRequest request) {
        UsuarioAcademico alumno = exigirRol(buscarPorDni(request.getAlumnoDni()), RolUsuario.ALUMNO);
        if (!mesPensionActiva(alumno, request.getAnio(), request.getMes())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El mes de pension no aplica para el periodo registrado del alumno");
        }
        PensionMensual pago = pensionMensualRepository
                .findByAlumno_DniAndAnioAndMes(alumno.getDni(), request.getAnio(), request.getMes())
                .orElseGet(() -> PensionMensual.builder()
                        .alumno(alumno)
                        .anio(request.getAnio())
                        .mes(request.getMes())
                        .build());
        pago.setPagada(Boolean.TRUE.equals(request.getPagada()));
        pago.setObservacion(request.getObservacion());
        PensionMensual saved = pensionMensualRepository.save(pago);
        return toPensionMensualDto(alumno, request.getAnio(), request.getMes(), saved, true);
    }

    private UsuarioAcademico buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioAcademico buscarPorDni(String dni) {
        return usuarioRepository.findByDni(dni)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioAcademico exigirRol(UsuarioAcademico usuario, RolUsuario rol) {
        if (!rol.equals(usuario.getRol())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no tiene rol " + rol);
        }
        return usuario;
    }

    private String normalizarTexto(String value) {
        return value == null ? "" : value.trim();
    }

    private void exigirAsignacionActiva(String docenteDni, String alumnoDni, CursoAcademico curso) {
        boolean asignado = asignacionRepository.findByDocente_DniAndActivoTrue(docenteDni).stream()
                .anyMatch(asignacion -> asignacion.getAlumno().getDni().equals(alumnoDni)
                        && (curso == null || asignacion.getCurso() == curso));
        if (!asignado) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El alumno o curso no esta asignado a este docente");
        }
    }

    private void aplicarPerfil(UsuarioAcademico usuario, UpdatePerfilAcademicoRequest request, boolean adminEdita) {
        if (adminEdita || RolUsuario.ALUMNO.equals(usuario.getRol())) {
            validarDatosUnicos(usuario.getId(), usuario.getDni(), request.getCodigo(), request.getCorreo());
        } else if (RolUsuario.DOCENTE.equals(usuario.getRol())) {
            validarDatosUnicos(usuario.getId(), usuario.getDni(), request.getCodigo(), request.getCorreo());
        }

        usuario.setNombre(request.getNombre());
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setCorreo(request.getCorreo());
        usuario.setDireccion(request.getDireccion());
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setTelefono(request.getTelefono());
        usuario.setFotoUrl(request.getFotoUrl());

        if (adminEdita) {
            validarDatosAcademicos(usuario.getRol(), request.getNivelEducativo(), request.getGrado());
            usuario.setCodigo(request.getCodigo());
            usuario.setNivelEducativo(request.getNivelEducativo());
            usuario.setGrado(request.getGrado());
            usuario.setSeccion(request.getSeccion());
            usuario.setEstadoMatricula(request.getEstadoMatricula());
        }
        if (adminEdita || RolUsuario.DOCENTE.equals(usuario.getRol())) {
            usuario.setCodigo(request.getCodigo());
            usuario.setMateria(request.getMateria());
            usuario.setEspecialidad(request.getEspecialidad());
        }
        if (adminEdita && request.getEstado() != null) {
            usuario.setEstado(request.getEstado());
            usuario.setActivo(EstadoUsuario.ACTIVO.equals(request.getEstado()));
        }
        if (adminEdita && RolUsuario.ALUMNO.equals(usuario.getRol())) {
            usuario.setPensionPagada(Boolean.TRUE.equals(request.getPensionPagada()));
            usuario.setPensionObservacion(request.getPensionObservacion());
        }
        if (request.getCreatedAt() != null) {
            usuario.setCreatedAt(request.getCreatedAt());
        }
        if (request.getInicioPeriodo() != null) {
            usuario.setInicioPeriodo(request.getInicioPeriodo());
        }
    }

    private void validarDatosUnicos(Long usuarioId, String dni, String codigo, String correo) {
        String dniNormalizado = normalizarTexto(dni);
        String codigoNormalizado = normalizarTexto(codigo);
        String correoNormalizado = normalizarTexto(correo);

        if (!dniNormalizado.isBlank() && (usuarioId == null
                ? usuarioRepository.existsByDni(dniNormalizado)
                : usuarioRepository.existsByDniAndIdNot(dniNormalizado, usuarioId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI ya esta registrado");
        }
        if (!codigoNormalizado.isBlank() && (usuarioId == null
                ? usuarioRepository.existsByCodigoIgnoreCase(codigoNormalizado)
                : usuarioRepository.existsByCodigoIgnoreCaseAndIdNot(codigoNormalizado, usuarioId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El codigo ya esta registrado");
        }
        if (!correoNormalizado.isBlank() && (usuarioId == null
                ? usuarioRepository.existsByCorreoIgnoreCase(correoNormalizado)
                : usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(correoNormalizado, usuarioId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya esta registrado");
        }
    }

    private String construirDependenciasEliminacion(UsuarioAcademico usuario) {
        List<String> dependencias = new ArrayList<>();
        String dni = usuario.getDni();

        long asignaciones = usuario.getRol() == RolUsuario.DOCENTE
                ? asignacionRepository.findByDocente_Dni(dni).size()
                : asignacionRepository.findByAlumno_Dni(dni).size();
        long asistencias = asistenciaRepository.countByAlumno_Dni(dni) + asistenciaRepository.countByDocente_Dni(dni);
        long notas = notaRepository.countByAlumno_Dni(dni) + notaRepository.countByDocente_Dni(dni);

        if (asignaciones > 0)
            dependencias.add(asignaciones + " asignaciones");
        if (asistencias > 0)
            dependencias.add(asistencias + " asistencias");
        if (notas > 0)
            dependencias.add(notas + " notas");

        if (RolUsuario.ALUMNO.equals(usuario.getRol())) {
            long pensiones = pensionMensualRepository.countByAlumno_Dni(dni);
            if (pensiones > 0)
                dependencias.add(pensiones + " pensiones");
        }

        if (dependencias.isEmpty()) {
            return "";
        }

        return "No se puede eliminar porque tiene datos vinculados: " + String.join(", ", dependencias) + ".";
    }

    private void validarDatosAcademicos(RolUsuario rol, NivelEducativo nivelEducativo, Grado grado) {
        if (!RolUsuario.ALUMNO.equals(rol)) {
            return;
        }
        if (nivelEducativo == null || grado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El alumno debe tener nivel educativo y grado");
        }
        boolean gradoPrimaria = grado.name().endsWith("_PRIMARIA");
        boolean gradoSecundaria = grado.name().endsWith("_SECUNDARIA");
        if ((NivelEducativo.PRIMARIA.equals(nivelEducativo) && !gradoPrimaria)
                || (NivelEducativo.SECUNDARIA.equals(nivelEducativo) && !gradoSecundaria)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El grado no corresponde al nivel educativo");
        }
    }

    private List<CursoAcademico> cursosParaAsignacionAula(AsignacionAulaRequest request) {
        if (NivelEducativo.PRIMARIA.equals(request.getNivelEducativo())) {
            return request.getCurso() == null
                    ? Arrays.asList(CursoAcademico.values())
                    : List.of(request.getCurso());
        }
        if (request.getCurso() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "En secundaria debes seleccionar el curso del docente especialista");
        }
        return List.of(request.getCurso());
    }

    private void validarAlumnoEnAula(UsuarioAcademico alumno, NivelEducativo nivelEducativo, Grado grado,
            com.monserrat.entity.Seccion seccion) {
        if (!Objects.equals(alumno.getNivelEducativo(), nivelEducativo)
                || !Objects.equals(alumno.getGrado(), grado)
                || !Objects.equals(alumno.getSeccion(), seccion)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El alumno no pertenece al aula seleccionada");
        }
    }

    private void validarDocenteCurso(UsuarioAcademico docente, NivelEducativo nivelEducativo, CursoAcademico curso) {
        if (NivelEducativo.PRIMARIA.equals(nivelEducativo)) {
            String materia = normalizarTexto(docente.getMateria());
            if (!materia.isBlank() && curso == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "En primaria debes seleccionar la competencia (área curricular) para el docente especialista");
            }
            if (!materia.isBlank() && curso != null && !materia.equalsIgnoreCase(curso.name())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El docente de primaria no corresponde a la competencia seleccionada");
            }
            return;
        }
        String materia = normalizarTexto(docente.getMateria());
        if (!materia.isBlank() && curso != null && !materia.equalsIgnoreCase(curso.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El docente especialista no corresponde al curso seleccionado");
        }
    }

    private void validarDocentePrimariaUnSoloSalon(UsuarioAcademico docente, AsignacionAulaRequest request) {
        if (!NivelEducativo.PRIMARIA.equals(request.getNivelEducativo())) {
            return;
        }
        String materia = normalizarTexto(docente.getMateria());
        if (!materia.isBlank()) {
            return;
        }
        boolean tieneOtroSalon = asignacionRepository.findByDocente_DniAndActivoTrue(docente.getDni()).stream()
                .anyMatch(asignacion -> NivelEducativo.PRIMARIA.equals(asignacion.getNivelEducativo())
                        && (!Objects.equals(asignacion.getGrado(), request.getGrado())
                                || !Objects.equals(asignacion.getSeccion(), request.getSeccion())));
        if (tieneOtroSalon) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El docente de primaria ya esta asignado a otro salon activo");
        }
    }

    private List<Integer> mesesDePensionParaAnio(UsuarioAcademico alumno, int anio) {
        LocalDateTime createdAt = alumno.getCreatedAt();
        if (createdAt == null) {
            return java.util.stream.IntStream.rangeClosed(1, 12).boxed().toList();
        }

        return java.util.stream.IntStream.rangeClosed(1, 12)
                .filter(mes -> esMesPensionActiva(createdAt, anio, mes, LocalDate.now()))
                .boxed()
                .toList();
    }

    static boolean esMesPensionActiva(LocalDateTime fechaInicioPeriodo, int anio, int mes, LocalDate fechaReferencia) {
        if (fechaInicioPeriodo == null) {
            return true;
        }
        LocalDate fechaInicio = fechaInicioPeriodo.toLocalDate();
        LocalDate referencia = fechaReferencia == null ? LocalDate.now() : fechaReferencia;

        if (anio < fechaInicio.getYear() || anio > referencia.getYear()) {
            return false;
        }
        if (anio == fechaInicio.getYear() && mes < fechaInicio.getMonthValue()) {
            return false;
        }
        if (anio == referencia.getYear() && mes > referencia.getMonthValue()) {
            return false;
        }
        return true;
    }

    private boolean mesPensionActiva(UsuarioAcademico alumno, int anio, int mes) {
        LocalDateTime fecha = alumno.getInicioPeriodo() != null
                ? alumno.getInicioPeriodo()
                : alumno.getCreatedAt();
        return esMesPensionActiva(fecha, anio, mes, LocalDate.now());
    }

    private UsuarioAcademicoDTO toUsuarioDto(UsuarioAcademico usuario) {
        return UsuarioAcademicoDTO.builder()
                .id(usuario.getId())
                .dni(usuario.getDni())
                .codigo(usuario.getCodigo())
                .nombre(usuario.getNombre())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .direccion(usuario.getDireccion())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .activo(usuario.getActivo())
                .telefono(usuario.getTelefono())
                .fotoUrl(usuario.getFotoUrl())
                .nivelEducativo(usuario.getNivelEducativo())
                .grado(usuario.getGrado())
                .seccion(usuario.getSeccion())
                .materia(usuario.getMateria())
                .especialidad(usuario.getEspecialidad())
                .estadoMatricula(usuario.getEstadoMatricula())
                .pensionPagada(usuario.getPensionPagada())
                .pensionObservacion(usuario.getPensionObservacion())
                .createdAt(usuario.getCreatedAt())
                .inicioPeriodo(usuario.getInicioPeriodo())
                .build();
    }

    private PensionMensualDTO toPensionMensualDto(UsuarioAcademico alumno, Integer anio, Integer mes,
            PensionMensual pago, boolean activa) {
        return PensionMensualDTO.builder()
                .alumnoDni(alumno.getDni())
                .alumnoCodigo(alumno.getCodigo())
                .alumnoNombre(alumno.getNombre())
                .nivelEducativo(alumno.getNivelEducativo())
                .grado(alumno.getGrado())
                .seccion(alumno.getSeccion())
                .anio(anio)
                .mes(mes)
                .pagada(pago != null && Boolean.TRUE.equals(pago.getPagada()))
                .activa(activa)
                .observacion(pago == null ? null : pago.getObservacion())
                .actualizadoEn(pago == null ? null : pago.getUpdatedAt())
                .build();
    }

    private PerfilAcademicoDTO toPerfilDto(UsuarioAcademico usuario) {
        return PerfilAcademicoDTO.builder()
                .id(usuario.getId())
                .dni(usuario.getDni())
                .codigo(usuario.getCodigo())
                .nombre(usuario.getNombre())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .direccion(usuario.getDireccion())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .estado(usuario.getEstado())
                .debeCambiarContrasena(usuario.getDebeCambiarContrasena())
                .telefono(usuario.getTelefono())
                .fotoUrl(usuario.getFotoUrl())
                .nivelEducativo(usuario.getNivelEducativo())
                .grado(usuario.getGrado())
                .seccion(usuario.getSeccion())
                .materia(usuario.getMateria())
                .especialidad(usuario.getEspecialidad())
                .estadoMatricula(usuario.getEstadoMatricula())
                .pensionPagada(usuario.getPensionPagada())
                .pensionObservacion(usuario.getPensionObservacion())
                .createdAt(usuario.getCreatedAt())
                .inicioPeriodo(usuario.getInicioPeriodo())
                .build();
    }

    private AsistenciaAcademicaDTO toAsistenciaDto(AsistenciaAcademica asistencia) {
        return AsistenciaAcademicaDTO.builder()
                .id(asistencia.getId())
                .alumnoId(asistencia.getAlumno().getId())
                .alumnoDni(asistencia.getAlumno().getDni())
                .alumnoNombre(asistencia.getAlumno().getNombre())
                .docenteId(asistencia.getDocente().getId())
                .docenteDni(asistencia.getDocente().getDni())
                .docenteNombre(asistencia.getDocente().getNombre())
                .fecha(asistencia.getFecha())
                .estado(asistencia.getEstado())
                .observacion(asistencia.getObservacion())
                .createdAt(asistencia.getCreatedAt())
                .build();
    }

    private NotaAcademicaDTO toNotaDto(NotaAcademica nota) {
        return NotaAcademicaDTO.builder()
                .id(nota.getId())
                .alumnoId(nota.getAlumno().getId())
                .alumnoDni(nota.getAlumno().getDni())
                .alumnoNombre(nota.getAlumno().getNombre())
                .docenteId(nota.getDocente().getId())
                .docenteDni(nota.getDocente().getDni())
                .docenteNombre(nota.getDocente().getNombre())
                .curso(nota.getCurso())
                .periodo(nota.getPeriodo())
                .tipoEvaluacion(nota.getTipoEvaluacion())
                .valor(nota.getValor())
                .observacion(nota.getObservacion())
                .competenciaId(nota.getCompetenciaId())
                .createdAt(nota.getCreatedAt())
                .updatedAt(nota.getUpdatedAt())
                .build();
    }

    private AsignacionAcademicaDTO toAsignacionDto(AsignacionAcademica asignacion) {
        return AsignacionAcademicaDTO.builder()
                .id(asignacion.getId())
                .docenteId(asignacion.getDocente().getId())
                .docenteDni(asignacion.getDocente().getDni())
                .docenteNombre(asignacion.getDocente().getNombre())
                .alumnoId(asignacion.getAlumno().getId())
                .alumnoDni(asignacion.getAlumno().getDni())
                .alumnoNombre(asignacion.getAlumno().getNombre())
                .curso(asignacion.getCurso())
                .nivelEducativo(asignacion.getNivelEducativo())
                .grado(asignacion.getGrado())
                .seccion(asignacion.getSeccion())
                .activo(asignacion.getActivo())
                .createdAt(asignacion.getCreatedAt())
                .updatedAt(asignacion.getUpdatedAt())
                .build();
    }
}
