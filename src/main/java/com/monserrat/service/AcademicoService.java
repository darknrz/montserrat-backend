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
import com.monserrat.entity.PeriodoBimestre;
import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.Seccion;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.service.AcademicoServiceHelper;
import com.monserrat.repository.AsignacionAcademicaRepository;
import com.monserrat.repository.AsistenciaAcademicaRepository;
import com.monserrat.repository.NotaAcademicaRepository;
import com.monserrat.repository.PensionMensualRepository;
import com.monserrat.repository.PeriodoBimestreRepository;
import com.monserrat.repository.UsuarioAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AcademicoService {

    private static final String CHATBOT_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CHATBOT_CODE_LENGTH = 8;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioAcademicoRepository usuarioRepository;
    private final AsignacionAcademicaRepository asignacionRepository;
    private final AsistenciaAcademicaRepository asistenciaRepository;
    private final NotaAcademicaRepository notaRepository;
    private final PensionMensualRepository pensionMensualRepository;
    private final PeriodoBimestreRepository periodoBimestreRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.monserrat.repository.CatalogoAcademicoRepository catalogoRepository;

    @Transactional
    public List<UsuarioAcademicoDTO> listarUsuarios() {
        List<UsuarioAcademico> usuarios = usuarioRepository.findAll();
        asegurarCodigosChatbot(usuarios);
        return usuarios.stream().map(this::toUsuarioDto).toList();
    }

    @Transactional
    public List<UsuarioAcademicoDTO> listarAlumnos() {
        List<UsuarioAcademico> alumnos = usuarioRepository.findByRolAndActivoTrue(RolUsuario.ALUMNO);
        asegurarCodigosChatbot(alumnos);
        return alumnos.stream().map(this::toUsuarioDto).toList();
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

    /**
     * Genera automáticamente un código único para alumno o docente
     * Alumnos: {año_inicio_período}{3_dígitos}{1_letra} (ej: 2025453E)
     * Docentes: DOC{3_dígitos} (ej: DOC234)
     */
    private String generarCodigoAutomatico(RolUsuario rol, LocalDateTime inicioPeriodo) {
        if (RolUsuario.ALUMNO.equals(rol)) {
            // Para alumnos: formato {año}{3_dígitos}{1_letra}
            int año = inicioPeriodo != null ? inicioPeriodo.getYear() : LocalDateTime.now().getYear();
            
            // Obtener el número secuencial más alto para alumnos de este año
            List<UsuarioAcademico> alumnosAño = usuarioRepository.findAll().stream()
                    .filter(u -> RolUsuario.ALUMNO.equals(u.getRol()) && u.getCodigo() != null && u.getCodigo().startsWith(String.valueOf(año)))
                    .collect(Collectors.toList());
            
            int numSecuencial = 1;
            char letra = 'A';
            
            if (!alumnosAño.isEmpty()) {
                // Extraer el número máximo existente
                for (UsuarioAcademico a : alumnosAño) {
                    String cod = a.getCodigo();
                    if (cod.length() >= 8) {
                        try {
                            int num = Integer.parseInt(cod.substring(4, 7));
                            char let = cod.charAt(7);
                            if (num >= numSecuencial || (num == numSecuencial && let >= letra)) {
                                numSecuencial = num;
                                letra = let;
                            }
                        } catch (Exception e) {
                            // Ignorar códigos mal formados
                        }
                    }
                }
                
                // Incrementar
                numSecuencial++;
                if (numSecuencial > 999) {
                    numSecuencial = 1;
                    letra = (char) (((letra - 'A' + 1) % 26) + 'A');
                }
            }
            
            return String.format("%d%03d%c", año, numSecuencial, letra);
        } else if (RolUsuario.DOCENTE.equals(rol)) {
            // Para docentes: DOC{3_dígitos}
            List<UsuarioAcademico> docentes = usuarioRepository.findAll().stream()
                    .filter(u -> RolUsuario.DOCENTE.equals(u.getRol()) && u.getCodigo() != null && u.getCodigo().startsWith("DOC"))
                    .sorted(Comparator.comparing(u -> u.getCodigo(), (c1, c2) -> {
                        try {
                            int n1 = Integer.parseInt(c1.substring(3));
                            int n2 = Integer.parseInt(c2.substring(3));
                            return Integer.compare(n2, n1); // Orden descendente
                        } catch (Exception e) {
                            return 0;
                        }
                    }))
                    .toList();
            
            int numDocente = 1;
            if (!docentes.isEmpty()) {
                try {
                    numDocente = Integer.parseInt(docentes.get(0).getCodigo().substring(3)) + 1;
                } catch (Exception e) {
                    numDocente = 1;
                }
            }
            
            if (numDocente > 999) {
                numDocente = 1;
            }
            
            return String.format("DOC%03d", numDocente);
        }
        
        return null;
    }

    public UsuarioAcademicoDTO crearUsuario(CreateUsuarioAcademicoRequest request) {
        RolUsuario rol = request.getRol() == null ? RolUsuario.ALUMNO : request.getRol();
        if (RolUsuario.ADMIN.equals(rol)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El administrador se gestiona desde la tabla de administradores");
        }
        validarDatosAcademicos(rol, request.getNivelEducativo(), request.getGrado());
        
        // Generar código automático si no se proporciona
        String codigoFinal = request.getCodigo();
        if (codigoFinal == null || codigoFinal.isBlank()) {
            codigoFinal = generarCodigoAutomatico(rol, request.getInicioPeriodo());
        }
        
        validarDatosUnicos(null, request.getDni(), codigoFinal, request.getCorreo());

        UsuarioAcademico usuario = UsuarioAcademico.builder()
                .dni(request.getDni())
                .codigo(codigoFinal)
                .codigoChatbot(generarCodigoChatbotUnico())
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

        UsuarioAcademico saved = usuarioRepository.save(usuario);
        if (RolUsuario.ALUMNO.equals(saved.getRol()) && saved.getNivelEducativo() != null && saved.getGrado() != null && saved.getSeccion() != null) {
            replicarAsignacionesDeAulaParaAlumno(saved);
        }
        return toUsuarioDto(saved);
    }

    @Transactional
    public UsuarioAcademicoDTO actualizarUsuario(Long id, UpdatePerfilAcademicoRequest request) {
        UsuarioAcademico usuario = buscarPorId(id);
        NivelEducativo nivelAnt = usuario.getNivelEducativo();
        Grado gradoAnt = usuario.getGrado();
        com.monserrat.entity.Seccion seccionAnt = usuario.getSeccion();

        aplicarPerfil(usuario, request, true);
        UsuarioAcademico saved = usuarioRepository.save(usuario);

        if (RolUsuario.ALUMNO.equals(saved.getRol())) {
            boolean cambioAula = nivelAnt != saved.getNivelEducativo() || gradoAnt != saved.getGrado() || seccionAnt != saved.getSeccion();
            if (cambioAula) {
                asignacionRepository.deleteByAlumno_Dni(saved.getDni());
                if (saved.getNivelEducativo() != null && saved.getGrado() != null && saved.getSeccion() != null) {
                    replicarAsignacionesDeAulaParaAlumno(saved);
                }
            }
        }
        return toUsuarioDto(saved);
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
        UsuarioAcademico usuario = buscarPorDni(dni);
        asegurarCodigoChatbot(usuario);
        return toPerfilDto(usuarioRepository.save(usuario));
    }

    @Transactional
    public PerfilAcademicoDTO regenerarCodigoChatbot(String dni) {
        UsuarioAcademico usuario = exigirRol(buscarPorDni(dni), RolUsuario.ALUMNO);
        usuario.setCodigoChatbot(generarCodigoChatbotUnico());
        return toPerfilDto(usuarioRepository.save(usuario));
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
public List<NotaAcademicaDTO> listarTodasLasNotas() {
    return notaRepository.findAll().stream()
            .sorted(Comparator.comparing(NotaAcademica::getUpdatedAt).reversed())
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
                .peek(a -> {
                    // Asegurar que grado y sección se obtengan del alumno si están nulos en la asignación
                    if (a.getGrado() == null && a.getAlumno() != null) {
                        a.setGrado(a.getAlumno().getGrado());
                    }
                    if (a.getSeccion() == null && a.getAlumno() != null) {
                        a.setSeccion(a.getAlumno().getSeccion());
                    }
                    if (a.getNivelEducativo() == null && a.getAlumno() != null) {
                        a.setNivelEducativo(a.getAlumno().getNivelEducativo());
                    }
                })
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
                .peek(a -> {
                    // Asegurar que grado y sección se obtengan del alumno si están nulos en la asignación
                    if (a.getGrado() == null && a.getAlumno() != null) {
                        a.setGrado(a.getAlumno().getGrado());
                    }
                    if (a.getSeccion() == null && a.getAlumno() != null) {
                        a.setSeccion(a.getAlumno().getSeccion());
                    }
                    if (a.getNivelEducativo() == null && a.getAlumno() != null) {
                        a.setNivelEducativo(a.getAlumno().getNivelEducativo());
                    }
                })
                .sorted(Comparator
                        .comparing((AsignacionAcademica a) -> a.getCurso().name(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(a -> a.getAlumno().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .map(this::toAsignacionDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsignacionAcademicaDTO> listarAsignacionesAlumno(String alumnoDni) {
        return asignacionRepository.findByAlumno_DniAndActivoTrue(alumnoDni).stream()
                .peek(a -> {
                    // Asegurar que grado y sección se obtengan del alumno si están nulos en la asignación
                    if (a.getGrado() == null && a.getAlumno() != null) {
                        a.setGrado(a.getAlumno().getGrado());
                    }
                    if (a.getSeccion() == null && a.getAlumno() != null) {
                        a.setSeccion(a.getAlumno().getSeccion());
                    }
                    if (a.getNivelEducativo() == null && a.getAlumno() != null) {
                        a.setNivelEducativo(a.getAlumno().getNivelEducativo());
                    }
                })
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

    // ============ MÉTODOS PARA GESTIÓN DE PERÍODOS BIMESTRALES ============

    @Transactional(readOnly = true)
    public List<PeriodoBimestreDTO> listarPeriodosBimestres(Integer anio) {
        if (anio == null) {
            anio = java.time.Year.now().getValue();
        }
        return periodoBimestreRepository.findByAnioOrderByNumeroBimestreAsc(anio).stream()
                .map(this::toPeriodoBimestreDto)
                .toList();
    }

    @Transactional
    public PeriodoBimestreDTO crearPeriodoBimestre(PeriodoBimestreRequest request) {
        // Validación: fechaInicio < fechaFin
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        // Validación: no duplicar números de bimestre para el mismo año
        periodoBimestreRepository.findByAnioAndNumeroBimestre(request.getAnio(), request.getNumeroBimestre())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya existe un bimestre " + request.getNumeroBimestre() + " para el año " + request.getAnio());
                });

        PeriodoBimestre periodo = PeriodoBimestre.builder()
                .anio(request.getAnio())
                .numeroBimestre(request.getNumeroBimestre())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .build();
        
        PeriodoBimestre saved = periodoBimestreRepository.save(periodo);
        return toPeriodoBimestreDto(saved);
    }

    @Transactional
    public PeriodoBimestreDTO actualizarPeriodoBimestre(Long id, PeriodoBimestreRequest request) {
        PeriodoBimestre periodo = periodoBimestreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Período no encontrado"));

        // Validación: fechaInicio < fechaFin
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        // Validación: no duplicar números de bimestre (excepto para el mismo ID)
        periodoBimestreRepository.findByAnioAndNumeroBimestre(request.getAnio(), request.getNumeroBimestre())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Ya existe un bimestre " + request.getNumeroBimestre() + " para el año " + request.getAnio());
                    }
                });

        periodo.setAnio(request.getAnio());
        periodo.setNumeroBimestre(request.getNumeroBimestre());
        periodo.setFechaInicio(request.getFechaInicio());
        periodo.setFechaFin(request.getFechaFin());
        
        PeriodoBimestre updated = periodoBimestreRepository.save(periodo);
        return toPeriodoBimestreDto(updated);
    }

    @Transactional
    public void eliminarPeriodoBimestre(Long id) {
        PeriodoBimestre periodo = periodoBimestreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Período no encontrado"));
        periodoBimestreRepository.deleteById(id);
    }

    private PeriodoBimestreDTO toPeriodoBimestreDto(PeriodoBimestre periodo) {
        return PeriodoBimestreDTO.builder()
                .id(periodo.getId())
                .anio(periodo.getAnio())
                .numeroBimestre(periodo.getNumeroBimestre())
                .fechaInicio(periodo.getFechaInicio())
                .fechaFin(periodo.getFechaFin())
                .createdAt(periodo.getCreatedAt())
                .updatedAt(periodo.getUpdatedAt())
                .build();
    }

    private UsuarioAcademico buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioAcademico buscarPorDni(String dni) {
        return usuarioRepository.findByDni(dni)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void asegurarCodigoChatbot(UsuarioAcademico usuario) {
        if (RolUsuario.ALUMNO.equals(usuario.getRol())
                && (usuario.getCodigoChatbot() == null || usuario.getCodigoChatbot().isBlank())) {
            usuario.setCodigoChatbot(generarCodigoChatbotUnico());
        }
    }

    private void asegurarCodigosChatbot(List<UsuarioAcademico> usuarios) {
        boolean changed = false;
        for (UsuarioAcademico usuario : usuarios) {
            String current = usuario.getCodigoChatbot();
            asegurarCodigoChatbot(usuario);
            changed = changed || !Objects.equals(current, usuario.getCodigoChatbot());
        }
        if (changed) {
            usuarioRepository.saveAll(usuarios);
        }
    }

    private String generarCodigoChatbotUnico() {
        String codigo;
        do {
            StringBuilder builder = new StringBuilder(CHATBOT_CODE_LENGTH);
            for (int i = 0; i < CHATBOT_CODE_LENGTH; i++) {
                builder.append(CHATBOT_CODE_ALPHABET.charAt(SECURE_RANDOM.nextInt(CHATBOT_CODE_ALPHABET.length())));
            }
            codigo = builder.toString();
        } while (usuarioRepository.existsByCodigoChatbotIgnoreCase(codigo));
        return codigo;
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

        if (request.getNombre() != null) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getNombres() != null) {
            usuario.setNombres(request.getNombres());
        }
        if (request.getApellidos() != null) {
            usuario.setApellidos(request.getApellidos());
        }
        if (request.getCorreo() != null) {
            usuario.setCorreo(request.getCorreo());
        }
        if (request.getDireccion() != null) {
            usuario.setDireccion(request.getDireccion());
        }
        if (request.getFechaNacimiento() != null) {
            usuario.setFechaNacimiento(request.getFechaNacimiento());
        }
        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }
        if (request.getFotoUrl() != null) {
            usuario.setFotoUrl(request.getFotoUrl());
        }

        if (adminEdita) {
            validarDatosAcademicos(usuario.getRol(), request.getNivelEducativo(), request.getGrado());
            if (request.getCodigo() != null) {
                usuario.setCodigo(request.getCodigo());
            }
            if (request.getNivelEducativo() != null) {
                usuario.setNivelEducativo(request.getNivelEducativo());
            }
            if (request.getGrado() != null) {
                usuario.setGrado(request.getGrado());
            }
            if (request.getSeccion() != null) {
                usuario.setSeccion(request.getSeccion());
            }
            if (request.getEstadoMatricula() != null) {
                usuario.setEstadoMatricula(request.getEstadoMatricula());
            }
        }
        if (adminEdita || RolUsuario.DOCENTE.equals(usuario.getRol())) {
            if (request.getCodigo() != null) {
                usuario.setCodigo(request.getCodigo());
            }
            if (request.getMateria() != null) {
                usuario.setMateria(request.getMateria());
            }
            if (request.getEspecialidad() != null) {
                usuario.setEspecialidad(request.getEspecialidad());
            }
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
        // Intenta obtener los períodos configurados para el año
        List<PeriodoBimestre> periodos = periodoBimestreRepository.findByAnioOrderByNumeroBimestreAsc(anio);
        
        if (!periodos.isEmpty()) {
            // Si hay períodos configurados, usa esos
            return java.util.stream.IntStream.rangeClosed(1, 12)
                    .filter(mes -> esMesPensionActivaConPeriodo(alumno, anio, mes, periodos))
                    .boxed()
                    .toList();
        }

        // Si no hay períodos, usa el método tradicional basado en createdAt/inicioPeriodo
        LocalDateTime createdAt = alumno.getCreatedAt();
        if (createdAt == null) {
            return java.util.stream.IntStream.rangeClosed(1, 12).boxed().toList();
        }

        return java.util.stream.IntStream.rangeClosed(1, 12)
                .filter(mes -> esMesPensionActiva(createdAt, anio, mes, LocalDate.now()))
                .boxed()
                .toList();
    }

    private boolean esMesPensionActivaConPeriodo(UsuarioAcademico alumno, int anio, int mes, List<PeriodoBimestre> periodos) {
        LocalDate fechaVerificacion = LocalDate.of(anio, mes, 1);
        LocalDate hoy = LocalDate.now();
        LocalDate fechaInicioAlumno = getInicioPeriodoLocalDate(alumno);

        if (fechaInicioAlumno != null && fechaVerificacion.isBefore(fechaInicioAlumno.withDayOfMonth(1))) {
            return false;
        }

        // El mes debe estar en un rango de período y no debe ser futuro
        for (PeriodoBimestre periodo : periodos) {
            if (!fechaVerificacion.isBefore(periodo.getFechaInicio())
                    && !fechaVerificacion.isAfter(periodo.getFechaFin())) {
                return !fechaVerificacion.isAfter(hoy);
            }
        }
        return false;
    }

    private LocalDate getInicioPeriodoLocalDate(UsuarioAcademico alumno) {
        LocalDateTime inicioPeriodo = alumno.getInicioPeriodo() != null ? alumno.getInicioPeriodo() : alumno.getCreatedAt();
        return inicioPeriodo == null ? null : inicioPeriodo.toLocalDate();
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
        // Intenta obtener los períodos configurados
        List<PeriodoBimestre> periodos = periodoBimestreRepository.findByAnioOrderByNumeroBimestreAsc(anio);
        
        if (!periodos.isEmpty()) {
            // Si hay períodos configurados, usa esos y también respeta el inicio del alumno
            return esMesPensionActivaConPeriodo(alumno, anio, mes, periodos);
        }

        // Si no hay períodos, usa el método tradicional
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
                .codigoChatbot(usuario.getCodigoChatbot())
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
        Long alumnoId = nota.getAlumno() != null ? nota.getAlumno().getId() : null;
        String alumnoDni = nota.getAlumno() != null ? nota.getAlumno().getDni() : null;
        String alumnoNombre = nota.getAlumno() != null ? nota.getAlumno().getNombre() : null;
        Long docenteId = nota.getDocente() != null ? nota.getDocente().getId() : null;
        String docenteDni = nota.getDocente() != null ? nota.getDocente().getDni() : null;
        String docenteNombre = nota.getDocente() != null ? nota.getDocente().getNombre() : null;

        return NotaAcademicaDTO.builder()
            .id(nota.getId())
            .alumnoId(alumnoId)
            .alumnoDni(alumnoDni)
            .alumnoNombre(alumnoNombre)
            .docenteId(docenteId)
            .docenteDni(docenteDni)
            .docenteNombre(docenteNombre)
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
    private void replicarAsignacionesDeAulaParaAlumno(UsuarioAcademico alumno) {
        // 1. Replicar desde docentes por competencia en el catálogo (fuente de verdad autoritativa)
        if (alumno.getGrado() != null) {
            String gradePrefix = alumno.getGrado().name() + "||";
            java.util.List<com.monserrat.entity.CatalogoAcademico> mappings = catalogoRepository.findAll();
            
            java.util.Map<CursoAcademico, String> cursoDocenteDniMap = new java.util.HashMap<>();
            for (com.monserrat.entity.CatalogoAcademico mapping : mappings) {
                if ("DOCENTE_COMPETENCIA".equals(mapping.getTipo()) && 
                    Boolean.TRUE.equals(mapping.getActivo()) && 
                    mapping.getCodigo() != null && 
                    mapping.getCodigo().startsWith(gradePrefix)) {
                    
                    String[] parts = mapping.getCodigo().split("\\|\\|");
                    if (parts.length >= 2) {
                        try {
                            CursoAcademico curso = CursoAcademico.valueOf(parts[1]);
                            String docenteDni = mapping.getNombre();
                            if (docenteDni != null && !docenteDni.isBlank()) {
                                cursoDocenteDniMap.put(curso, docenteDni);
                            }
                        } catch (IllegalArgumentException e) {
                            // Ignorar si el curso no es válido en el enum
                        }
                    }
                }
            }

            // Crear asignaciones del catálogo
            for (java.util.Map.Entry<CursoAcademico, String> entry : cursoDocenteDniMap.entrySet()) {
                UsuarioAcademico docente = usuarioRepository.findByDni(entry.getValue()).orElse(null);
                if (docente != null) {
                    boolean yaExiste = asignacionRepository.existsByDocente_DniAndAlumno_DniAndCursoAndGradoAndSeccionAndActivoTrue(
                            docente.getDni(), alumno.getDni(), entry.getKey(), alumno.getGrado(), alumno.getSeccion());
                    if (!yaExiste) {
                        AsignacionAcademica nuevaAsignacion = AsignacionAcademica.builder()
                                .docente(docente)
                                .alumno(alumno)
                                .curso(entry.getKey())
                                .nivelEducativo(alumno.getNivelEducativo())
                                .grado(alumno.getGrado())
                                .seccion(alumno.getSeccion())
                                .activo(true)
                                .build();
                        asignacionRepository.save(nuevaAsignacion);
                    }
                }
            }
        }

        // 2. Replicar desde asignaciones de aula existentes (como fallback secundario)
        List<AsignacionAcademica> asignacionesExistentes = asignacionRepository
                .findByNivelEducativoAndGradoAndSeccionAndActivoTrue(
                        alumno.getNivelEducativo(), alumno.getGrado(), alumno.getSeccion());

        java.util.Map<CursoAcademico, UsuarioAcademico> cursoDocenteMap = new java.util.HashMap<>();
        for (AsignacionAcademica asig : asignacionesExistentes) {
            cursoDocenteMap.put(asig.getCurso(), asig.getDocente());
        }

        for (java.util.Map.Entry<CursoAcademico, UsuarioAcademico> entry : cursoDocenteMap.entrySet()) {
            boolean yaExiste = asignacionRepository.existsByDocente_DniAndAlumno_DniAndCursoAndGradoAndSeccionAndActivoTrue(
                    entry.getValue().getDni(), alumno.getDni(), entry.getKey(), alumno.getGrado(), alumno.getSeccion());
            if (!yaExiste) {
                AsignacionAcademica nuevaAsignacion = AsignacionAcademica.builder()
                        .docente(entry.getValue())
                        .alumno(alumno)
                        .curso(entry.getKey())
                        .nivelEducativo(alumno.getNivelEducativo())
                        .grado(alumno.getGrado())
                        .seccion(alumno.getSeccion())
                        .activo(true)
                        .build();
                asignacionRepository.save(nuevaAsignacion);
            }
        }
    }

    @Transactional
    public ImportacionResultDTO importarUsuariosDesdeArchivo(org.springframework.web.multipart.MultipartFile file, String tipo) {
        List<String> errores = new ArrayList<>();
        int totalProcesados = 0;
        int exitosos = 0;
        int fallidos = 0;

        try {
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream());
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

            RolUsuario rol = "ALUMNO".equalsIgnoreCase(tipo) ? RolUsuario.ALUMNO : RolUsuario.DOCENTE;

            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;

                totalProcesados++;

                try {
                    // Leer valores del Excel - ajustado para formato del usuario
                    String nivel = getCellValue(row, 0).trim();              // Primaria/Secundaria
                    String nivelAcademico = getCellValue(row, 1).trim();    // Nivel académico o ignorar
                    String gradoStr = getCellValue(row, 2).trim();          // Grado
                    String inicioPeriodoStr = getCellValue(row, 3).trim();  // Fecha inicio
                    String nombreCompleto = getCellValue(row, 4).trim();    // Nombre del alumno

                    // Validaciones
                    if (nombreCompleto.isBlank()) {
                        errores.add("Fila " + (i + 1) + ": Nombre del alumno es requerido");
                        fallidos++;
                        continue;
                    }

                    // Parsear nombre completo para extraer DNI o generar valores
                    String[] partes = nombreCompleto.split("\\s+");
                    String apellidos = partes.length >= 2 ? String.join(" ", Arrays.copyOfRange(partes, 0, partes.length - 1)) : "";
                    String nombre = partes.length > 0 ? partes[partes.length - 1] : "";
                    
                    // Para el DNI en importación, usar hash del nombre como temporal
                    String dni = String.valueOf((nombreCompleto + nivel + gradoStr).hashCode()).replace("-", "").substring(0, 8);
                    
                    // Validar que no exista usuario con mismo nombre+nivel+grado
                    if (usuarioRepository.findAll().stream()
                            .anyMatch(u -> u.getNombre().equalsIgnoreCase(nombreCompleto) && 
                                         u.getNivelEducativo().name().equalsIgnoreCase(nivel.toUpperCase()))) {
                        errores.add("Fila " + (i + 1) + ": " + nombreCompleto + " ya existe en el sistema");
                        fallidos++;
                        continue;
                    }

                    // Convertir nivel educativo
                    NivelEducativo nivelEducativo;
                    try {
                        nivelEducativo = NivelEducativo.valueOf(nivel.toUpperCase().replace(" ", "_"));
                    } catch (IllegalArgumentException e) {
                        errores.add("Fila " + (i + 1) + ": Nivel educativo inválido: " + nivel);
                        fallidos++;
                        continue;
                    }

                    // Convertir grado
                    Grado grado = AcademicoServiceHelper.parseGrado(gradoStr, nivelEducativo);
                    if (grado == null) {
                        errores.add("Fila " + (i + 1) + ": Grado inválido: " + gradoStr);
                        fallidos++;
                        continue;
                    }

                    // Parsear fecha de inicio del período
                    LocalDateTime fechaInicio = null;
                    if (!inicioPeriodoStr.isBlank()) {
                        try {
                            // Intentar parsear formato dd/MM/yyyy
                            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate fecha = LocalDate.parse(inicioPeriodoStr, formatter);
                            fechaInicio = fecha.atStartOfDay();
                        } catch (Exception parseError) {
                            // Si falla el parseo, dejar inicioPeriodo como null
                            fechaInicio = null;
                        }
                    }

                    // Generar código automático con la fecha del período (o fecha actual si no existe)
                    String codigoGenerado = generarCodigoAutomatico(rol, fechaInicio);

                    // Crear usuario
                    UsuarioAcademico usuario = UsuarioAcademico.builder()
                            .dni(dni)
                            .codigo(codigoGenerado)
                            .codigoChatbot(generarCodigoChatbotUnico())
                            .nombre(nombreCompleto)
                            .apellidos(apellidos)
                            .correo(nombreCompleto.toLowerCase().replace(" ", ".") + "@importado.edu")
                            .rol(rol)
                            .nivelEducativo(nivelEducativo)
                            .grado(grado)
                            .seccion(Seccion.A)  // Asignar sección A por defecto
                            .estado(EstadoUsuario.ACTIVO)
                            .activo(true)
                            .password(passwordEncoder.encode("123456")) // Contraseña por defecto
                            .debeCambiarContrasena(true)
                            .inicioPeriodo(fechaInicio)
                            .build();

                    usuarioRepository.save(usuario);
                    exitosos++;

                } catch (Exception e) {
                    errores.add("Fila " + (i + 1) + ": " + e.getMessage());
                    fallidos++;
                }
            }

            workbook.close();

        } catch (Exception e) {
            return ImportacionResultDTO.builder()
                    .totalProcesados(0)
                    .exitosos(0)
                    .fallidos(1)
                    .errores(Arrays.asList("Error al procesar archivo: " + e.getMessage()))
                    .mensaje("FALLO")
                    .build();
        }

        return ImportacionResultDTO.builder()
                .totalProcesados(totalProcesados)
                .exitosos(exitosos)
                .fallidos(fallidos)
                .errores(errores)
                .mensaje(exitosos > 0 ? "EXITOSO" : "FALLO")
                .build();
    }

    private String getCellValue(org.apache.poi.ss.usermodel.Row row, int cellIndex) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Verificar si es una fecha
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    java.time.LocalDateTime dateTime = cell.getLocalDateTimeCellValue();
                    return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
