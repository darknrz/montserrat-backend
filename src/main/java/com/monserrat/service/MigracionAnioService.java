package com.monserrat.service;

import com.monserrat.dto.anio.AccionMigracion;
import com.monserrat.dto.anio.MigracionDecisionDTO;
import com.monserrat.dto.anio.MigracionItemDTO;
import com.monserrat.dto.anio.MigracionPreviewDTO;
import com.monserrat.dto.anio.MigracionRequest;
import com.monserrat.dto.anio.MigracionResultadoDTO;
import com.monserrat.entity.AnioEscolar;
import com.monserrat.entity.EstadoAnioEscolar;
import com.monserrat.entity.EstadoMatricula;
import com.monserrat.entity.Grado;
import com.monserrat.entity.HistorialAlumnoAnio;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.PeriodoBimestre;
import com.monserrat.entity.ResultadoAnual;
import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.Seccion;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.repository.AnioEscolarRepository;
import com.monserrat.repository.AsignacionAcademicaRepository;
import com.monserrat.repository.HistorialAlumnoAnioRepository;
import com.monserrat.repository.PeriodoBimestreRepository;
import com.monserrat.repository.UsuarioAcademicoRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Migración manual de un año escolar al siguiente.
 *
 * <p>Diseño: los datos operativos (notas, asistencias, asignaciones docente-alumno) siempre pertenecen al
 * año activo. Al migrar se archivan en tablas históricas (con el año), se promueve a los alumnos y se
 * empieza el nuevo año limpio. Matrículas, pensiones y talleres ya están particionados por año.
 * Todo ocurre en una sola transacción: o se migra completo o no cambia nada.
 */
@Service
@RequiredArgsConstructor
public class MigracionAnioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigracionAnioService.class);
    private static final int MAX_ANIOS_ADELANTE = 5;

    private static final List<Seccion> SECCIONES_REGULARES = List.of(Seccion.A, Seccion.B, Seccion.C, Seccion.D);
    private static final Map<Grado, List<Seccion>> GRUPOS_POR_GRADO = new EnumMap<>(Grado.class);

    static {
        // Fuente: hoja "PyS por nivel" de Curricula_Monserrat.xlsx. Un salón agrupa varios grados.
        GRUPOS_POR_GRADO.put(Grado.SEXTO_PRIMARIA, List.of(Seccion.CICLADO_I, Seccion.CICLADO_II));
        GRUPOS_POR_GRADO.put(Grado.PRIMERO_SECUNDARIA, List.of(Seccion.CICLADO_I, Seccion.CICLADO_II, Seccion.ANUAL));
        GRUPOS_POR_GRADO.put(Grado.SEGUNDO_SECUNDARIA, List.of(Seccion.ANUAL));
        GRUPOS_POR_GRADO.put(Grado.TERCERO_SECUNDARIA, List.of(Seccion.ANUAL, Seccion.LETRAS, Seccion.CIENCIAS));
        GRUPOS_POR_GRADO.put(Grado.CUARTO_SECUNDARIA, List.of(Seccion.LETRAS, Seccion.CIENCIAS));
        GRUPOS_POR_GRADO.put(Grado.QUINTO_SECUNDARIA, List.of(Seccion.LETRAS, Seccion.CIENCIAS));
    }

    private final AnioEscolarRepository anioRepository;
    private final AnioEscolarService anioService;
    private final UsuarioAcademicoRepository usuarioRepository;
    private final AsignacionAcademicaRepository asignacionRepository;
    private final PeriodoBimestreRepository periodoRepository;
    private final HistorialAlumnoAnioRepository historialRepository;
    private final AcademicoService academicoService;
    private final EntityManager entityManager;

    // ───────────────────────── Vista previa (solo lectura) ─────────────────────────

    @Transactional(readOnly = true)
    public MigracionPreviewDTO vistaPrevia(MigracionRequest request) {
        AnioEscolar origen = anioRepository.findFirstByEstado(EstadoAnioEscolar.ACTIVO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No hay un año escolar activo"));
        validarDestino(origen, request.getAnioDestino());
        Plan plan = planificar(origen.getAnio(), request);

        boolean copiar = !Boolean.FALSE.equals(request.getCopiarBimestres());
        int bimestres = copiar && periodoRepository.findByAnio(request.getAnioDestino()).isEmpty()
                ? periodoRepository.findByAnio(origen.getAnio()).size() : 0;

        return MigracionPreviewDTO.builder()
                .anioOrigen(origen.getAnio())
                .anioDestino(request.getAnioDestino())
                .items(plan.items.stream().map(PlanItem::toDto).toList())
                .promovidos(plan.contar(AccionMigracion.PROMOVER))
                .repitentes(plan.contar(AccionMigracion.REPETIR))
                .egresados(plan.contar(AccionMigracion.EGRESAR))
                .retirados(plan.contar(AccionMigracion.RETIRAR))
                .pendientesSeccion((int) plan.items.stream().filter(PlanItem::faltaSeccion).count())
                .notasAArchivar(contarFilas("notas_academicas"))
                .asistenciasAArchivar(contarFilas("asistencias_academicas"))
                .bimestresACopiar(bimestres)
                .omitidos(plan.omitidos)
                .puedeEjecutar(plan.items.stream().noneMatch(PlanItem::faltaSeccion))
                .build();
    }

    // ───────────────────────── Ejecución (transaccional) ─────────────────────────

    @Transactional
    public MigracionResultadoDTO ejecutar(MigracionRequest request, String usuario) {
        if (request.getAnioDestino() == null
                || !("MIGRAR " + request.getAnioDestino()).equals(request.getConfirmacion() == null
                        ? null : request.getConfirmacion().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Confirmación inválida. Escribe exactamente: MIGRAR " + request.getAnioDestino());
        }

        // Bloqueo pesimista: dos migraciones simultáneas no pueden coexistir.
        AnioEscolar origen = anioRepository.findActivoForUpdate()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No hay un año escolar activo"));
        validarDestino(origen, request.getAnioDestino());

        Plan plan = planificar(origen.getAnio(), request);
        List<String> sinSeccion = plan.items.stream().filter(PlanItem::faltaSeccion)
                .map(i -> i.alumno.getNombre()).toList();
        if (!sinSeccion.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Falta elegir la sección/grupo de destino de " + sinSeccion.size() + " alumno(s): "
                            + String.join(", ", sinSeccion.stream().limit(5).toList())
                            + (sinSeccion.size() > 5 ? "…" : ""));
        }
        if (historialRepository.existsByAnio(origen.getAnio())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El año " + origen.getAnio() + " ya tiene historial archivado; revisa la base de datos");
        }

        LocalDateTime ahora = LocalDateTime.now();
        entityManager.flush();

        // 1) Archivar notas y asistencias del año que se cierra (antes de tocar grados/secciones).
        int notas = archivarNotas(origen.getAnio(), ahora);
        int asistencias = archivarAsistencias(origen.getAnio(), ahora);

        // 2) Historial de cada alumno (foto del grado/sección y resultado).
        List<HistorialAlumnoAnio> historial = new ArrayList<>();
        for (PlanItem item : plan.items) {
            UsuarioAcademico a = item.alumno;
            historial.add(HistorialAlumnoAnio.builder()
                    .anio(origen.getAnio())
                    .alumnoId(a.getId())
                    .alumnoDni(a.getDni())
                    .alumnoCodigo(a.getCodigo())
                    .alumnoNombre(a.getNombre())
                    .nivelEducativo(a.getNivelEducativo())
                    .grado(a.getGrado())
                    .seccion(a.getSeccion())
                    .resultado(item.resultado())
                    .gradoDestino(item.continua() ? item.gradoDestino : null)
                    .seccionDestino(item.continua() ? item.seccionDestino : null)
                    .build());
        }
        historialRepository.saveAll(historial);

        // 3) Limpiar datos operativos del año cerrado.
        entityManager.createNativeQuery("DELETE FROM notas_academicas").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM asistencias_academicas").executeUpdate();

        // 4) Aplicar cambios a los alumnos.
        List<UsuarioAcademico> aReasignar = new ArrayList<>();
        for (PlanItem item : plan.items) {
            UsuarioAcademico a = item.alumno;
            switch (item.accion) {
                case EGRESAR -> {
                    asignacionRepository.deleteByAlumno_Dni(a.getDni());
                    a.setEstadoMatricula(EstadoMatricula.EGRESADO);
                    a.setActivo(false);
                }
                case RETIRAR -> {
                    asignacionRepository.deleteByAlumno_Dni(a.getDni());
                    a.setEstadoMatricula(EstadoMatricula.RETIRADO);
                    a.setActivo(false);
                }
                case PROMOVER, REPETIR -> {
                    boolean cambioAula = a.getGrado() != item.gradoDestino
                            || !Objects.equals(a.getSeccion(), item.seccionDestino);
                    if (cambioAula) {
                        asignacionRepository.deleteByAlumno_Dni(a.getDni());
                        a.setNivelEducativo(item.nivelDestino);
                        a.setGrado(item.gradoDestino);
                        a.setSeccion(item.seccionDestino);
                        aReasignar.add(a);
                    }
                    a.setEstadoMatricula(EstadoMatricula.MATRICULADO);
                    // Banderas heredadas del esquema antiguo: se reinician para el nuevo año.
                    a.setPensionPagada(false);
                    a.setPensionObservacion(null);
                }
            }
        }
        usuarioRepository.saveAll(plan.items.stream().map(i -> i.alumno).toList());
        entityManager.flush();

        // 5) Reasignar docentes por catálogo en el nuevo grado (segunda pasada, ya con todo persistido).
        for (UsuarioAcademico alumno : aReasignar) {
            academicoService.replicarAsignacionesDeAulaParaAlumno(alumno);
        }
        entityManager.flush();
        int sinAsignaciones = (int) aReasignar.stream()
                .filter(a -> asignacionRepository.findByAlumno_DniAndActivoTrue(a.getDni()).isEmpty())
                .count();

        // 6) Bimestres del nuevo año.
        int bimestresCopiados = 0;
        if (!Boolean.FALSE.equals(request.getCopiarBimestres())
                && periodoRepository.findByAnio(request.getAnioDestino()).isEmpty()) {
            int delta = request.getAnioDestino() - origen.getAnio();
            List<PeriodoBimestre> copias = periodoRepository.findByAnioOrderByNumeroBimestreAsc(origen.getAnio())
                    .stream()
                    .map(p -> PeriodoBimestre.builder()
                            .anio(request.getAnioDestino())
                            .numeroBimestre(p.getNumeroBimestre())
                            .fechaInicio(p.getFechaInicio().plusYears(delta))
                            .fechaFin(p.getFechaFin().plusYears(delta))
                            .build())
                    .toList();
            periodoRepository.saveAll(copias);
            bimestresCopiados = copias.size();
        }

        // 7) Cerrar el año de origen y activar el destino. El cierre se flushea primero para respetar
        //    el índice único de "un solo año activo".
        int promovidos = plan.contar(AccionMigracion.PROMOVER);
        int repitentes = plan.contar(AccionMigracion.REPETIR);
        int egresados = plan.contar(AccionMigracion.EGRESAR);
        int retirados = plan.contar(AccionMigracion.RETIRAR);

        origen.setEstado(EstadoAnioEscolar.CERRADO);
        origen.setFechaCierre(ahora);
        origen.setCerradoPor(usuario);
        origen.setTotalPromovidos(promovidos);
        origen.setTotalRepitentes(repitentes);
        origen.setTotalEgresados(egresados);
        origen.setTotalRetirados(retirados);
        anioRepository.saveAndFlush(origen);

        AnioEscolar destino = anioRepository.findByAnio(request.getAnioDestino())
                .orElseGet(() -> AnioEscolar.builder().anio(request.getAnioDestino()).build());
        destino.setEstado(EstadoAnioEscolar.ACTIVO);
        anioRepository.saveAndFlush(destino);
        anioService.invalidarCache();

        LOGGER.info("Migración {} -> {} por {}: {} promovidos, {} repitentes, {} egresados, {} retirados, "
                        + "{} notas y {} asistencias archivadas",
                origen.getAnio(), request.getAnioDestino(), usuario, promovidos, repitentes, egresados,
                retirados, notas, asistencias);

        return MigracionResultadoDTO.builder()
                .anioOrigen(origen.getAnio())
                .anioDestino(request.getAnioDestino())
                .promovidos(promovidos)
                .repitentes(repitentes)
                .egresados(egresados)
                .retirados(retirados)
                .notasArchivadas(notas)
                .asistenciasArchivadas(asistencias)
                .bimestresCopiados(bimestresCopiados)
                .alumnosSinAsignaciones(sinAsignaciones)
                .build();
    }

    // ───────────────────────── Planificación ─────────────────────────

    private void validarDestino(AnioEscolar origen, Integer destino) {
        if (destino == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica el año escolar de destino");
        }
        if (destino <= origen.getAnio()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El año de destino debe ser posterior al año activo (" + origen.getAnio() + ")");
        }
        if (destino > origen.getAnio() + MAX_ANIOS_ADELANTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El año de destino es demasiado lejano");
        }
        anioRepository.findByAnio(destino).ifPresent(existente -> {
            if (existente.getEstado() == EstadoAnioEscolar.CERRADO) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El año " + destino + " ya fue cerrado");
            }
        });
    }

    private Plan planificar(int anioOrigen, MigracionRequest request) {
        Map<Long, MigracionDecisionDTO> decisiones = request.getDecisiones() == null ? Map.of()
                : request.getDecisiones().stream()
                        .filter(d -> d.getAlumnoId() != null)
                        .collect(Collectors.toMap(MigracionDecisionDTO::getAlumnoId, d -> d, (a, b) -> b));

        List<UsuarioAcademico> alumnos = usuarioRepository.findByRolAndActivoTrue(RolUsuario.ALUMNO).stream()
                .sorted(Comparator
                        .comparing((UsuarioAcademico a) -> a.getGrado() == null ? -1 : a.getGrado().ordinal())
                        .thenComparing(a -> a.getSeccion() == null ? "" : a.getSeccion().name())
                        .thenComparing(UsuarioAcademico::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Plan plan = new Plan();
        for (UsuarioAcademico a : alumnos) {
            if (a.getGrado() == null) {
                plan.omitidos.add(a.getNombre() + " (sin grado)");
                continue;
            }
            if (a.getEstadoMatricula() != null && a.getEstadoMatricula() != EstadoMatricula.MATRICULADO) {
                plan.omitidos.add(a.getNombre() + " (" + a.getEstadoMatricula() + ")");
                continue;
            }
            MigracionDecisionDTO decision = decisiones.get(a.getId());
            AccionMigracion accion = decision != null && decision.getAccion() != null
                    ? decision.getAccion()
                    : (a.getGrado() == Grado.QUINTO_SECUNDARIA ? AccionMigracion.EGRESAR : AccionMigracion.PROMOVER);
            plan.items.add(new PlanItem(a, accion, decision == null ? null : decision.getSeccion()));
        }
        for (PlanItem item : plan.items) {
            item.resolverDestino();
        }
        return plan;
    }

    static Grado siguienteGrado(Grado actual) {
        Grado[] todos = Grado.values();
        int next = actual.ordinal() + 1;
        return next < todos.length ? todos[next] : null;
    }

    static NivelEducativo nivelDeGrado(Grado grado) {
        if (grado == Grado.INICIAL) {
            return NivelEducativo.INICIAL;
        }
        return grado.name().endsWith("_PRIMARIA") ? NivelEducativo.PRIMARIA : NivelEducativo.SECUNDARIA;
    }

    /**
     * Peldaño del salón en la escalera académica (orden de NIVEL_ACADEMICO): Ciclado I, Ciclado II, Anual y
     * luego Letras/Ciencias (mismo peldaño: son pistas finales). Sin salón (A) equivale a antes de Ciclado I.
     */
    static int peldano(Seccion salon) {
        if (salon == null) {
            return 0;
        }
        return switch (salon) {
            case CICLADO_I -> 1;
            case CICLADO_II -> 2;
            case ANUAL -> 3;
            case LETRAS, CIENCIAS -> 4;
            default -> 0;
        };
    }

    /** Siguiente salón de la escalera permitido en el grado de destino; null si no es único. */
    static Seccion sugerirSalon(Seccion actual, List<Seccion> permitidas) {
        int actualPeldano = peldano(actual);
        int menor = permitidas.stream().mapToInt(MigracionAnioService::peldano)
                .filter(p -> p > actualPeldano).min().orElse(-1);
        if (menor < 0) {
            return null;
        }
        List<Seccion> candidatos = permitidas.stream().filter(sec -> peldano(sec) == menor).toList();
        return candidatos.size() == 1 ? candidatos.get(0) : null;
    }

    /** true si el grado se divide en grupos reales (Ciclado, Anual, Letras, Ciencias). */
    static boolean usaGrupo(Grado grado) {
        return GRUPOS_POR_GRADO.containsKey(grado);
    }

    static List<Seccion> seccionesPermitidas(Grado grado) {
        return GRUPOS_POR_GRADO.getOrDefault(grado, SECCIONES_REGULARES);
    }

    private static final class Plan {
        final List<PlanItem> items = new ArrayList<>();
        final List<String> omitidos = new ArrayList<>();

        int contar(AccionMigracion accion) {
            return (int) items.stream().filter(i -> i.accion == accion).count();
        }
    }

    private static final class PlanItem {
        final UsuarioAcademico alumno;
        final AccionMigracion accion;
        final Seccion seccionElegida;
        Grado gradoDestino;
        NivelEducativo nivelDestino;
        Seccion seccionDestino;
        boolean sugerido;

        PlanItem(UsuarioAcademico alumno, AccionMigracion accion, Seccion seccionElegida) {
            this.alumno = alumno;
            this.accion = accion;
            this.seccionElegida = seccionElegida;
        }

        boolean continua() {
            return accion == AccionMigracion.PROMOVER || accion == AccionMigracion.REPETIR;
        }

        boolean faltaSeccion() {
            return continua() && seccionDestino == null;
        }

        ResultadoAnual resultado() {
            return switch (accion) {
                case PROMOVER -> ResultadoAnual.PROMOVIDO;
                case REPETIR -> ResultadoAnual.REPITE;
                case EGRESAR -> ResultadoAnual.EGRESADO;
                case RETIRAR -> ResultadoAnual.RETIRADO;
            };
        }

        void resolverDestino() {
            if (!continua()) {
                return;
            }
            if (accion == AccionMigracion.PROMOVER) {
                gradoDestino = siguienteGrado(alumno.getGrado());
                if (gradoDestino == null || alumno.getGrado() == Grado.QUINTO_SECUNDARIA) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            alumno.getNombre() + " está en el último grado: usa la acción EGRESAR");
                }
            } else {
                gradoDestino = alumno.getGrado();
            }
            nivelDestino = nivelDeGrado(gradoDestino);
            List<Seccion> permitidas = seccionesPermitidas(gradoDestino);
            if (seccionElegida != null) {
                if (!permitidas.contains(seccionElegida)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "La sección " + seccionElegida + " no es válida para " + gradoDestino.getNombre()
                                    + " (alumno " + alumno.getNombre() + ")");
                }
                seccionDestino = seccionElegida;
            } else if (alumno.getSeccion() != null && permitidas.contains(alumno.getSeccion())) {
                seccionDestino = alumno.getSeccion();
            } else if (permitidas.size() == 1 && usaGrupo(gradoDestino)) {
                // Salón único para ese grado (2do secundaria solo tiene Anual): no hay nada que decidir.
                seccionDestino = permitidas.get(0);
            } else if (usaGrupo(gradoDestino)) {
                // El salón es un nivel académico que cruza grados: se sigue la escalera del catálogo
                // NIVEL_ACADEMICO. Si hay un único siguiente peldaño, se sugiere; si hay empate
                // (Letras vs Ciencias) lo decide el admin.
                seccionDestino = sugerirSalon(alumno.getSeccion(), permitidas);
                sugerido = seccionDestino != null;
            } else {
                // La institución no maneja secciones: "A" es solo el valor interno por defecto
                // de los grados sin grupo. Solo los grados con grupo requieren decisión del admin.
                seccionDestino = Seccion.A;
            }
        }

        MigracionItemDTO toDto() {
            return MigracionItemDTO.builder()
                    .alumnoId(alumno.getId())
                    .dni(alumno.getDni())
                    .codigo(alumno.getCodigo())
                    .nombre(alumno.getNombre())
                    .nivelActual(alumno.getNivelEducativo())
                    .gradoActual(alumno.getGrado())
                    .seccionActual(alumno.getSeccion())
                    .accion(accion)
                    .nivelDestino(continua() ? nivelDestino : null)
                    .gradoDestino(continua() ? gradoDestino : null)
                    .seccionDestino(continua() ? seccionDestino : null)
                    .seccionesPermitidas(continua() ? seccionesPermitidas(gradoDestino) : List.of())
                    .requiereSeccion(faltaSeccion())
                    .usaGrupo(continua() && usaGrupo(gradoDestino))
                    .salonSugerido(continua() && sugerido)
                    .build();
        }
    }

    // ───────────────────────── Archivado (SQL masivo) ─────────────────────────

    private int contarFilas(String tabla) {
        // Nombre de tabla fijo (nunca viene del usuario).
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + tabla).getSingleResult()).intValue();
    }

    private int archivarNotas(int anio, LocalDateTime ahora) {
        return entityManager.createNativeQuery(
                        "INSERT INTO notas_historicas (anio, alumno_id, alumno_dni, alumno_nombre, docente_id, "
                                + "docente_nombre, grado, seccion, curso, periodo, tipo_evaluacion, valor, observacion, "
                                + "competencia_id, registrado_en, archivado_en) "
                                + "SELECT :anio, n.alumno_id, a.dni, a.nombre, n.docente_id, d.nombre, a.grado, a.seccion, "
                                + "n.curso, n.periodo, n.tipo_evaluacion, n.valor, n.observacion, n.competencia_id, "
                                + "n.created_at, :ahora "
                                + "FROM notas_academicas n "
                                + "JOIN usuarios_academicos a ON a.id = n.alumno_id "
                                + "LEFT JOIN usuarios_academicos d ON d.id = n.docente_id")
                .setParameter("anio", anio)
                .setParameter("ahora", ahora)
                .executeUpdate();
    }

    private int archivarAsistencias(int anio, LocalDateTime ahora) {
        return entityManager.createNativeQuery(
                        "INSERT INTO asistencias_historicas (anio, alumno_id, alumno_dni, alumno_nombre, docente_id, "
                                + "docente_nombre, grado, seccion, fecha, estado, observacion, archivado_en) "
                                + "SELECT :anio, s.alumno_id, a.dni, a.nombre, s.docente_id, d.nombre, a.grado, a.seccion, "
                                + "s.fecha, s.estado, s.observacion, :ahora "
                                + "FROM asistencias_academicas s "
                                + "JOIN usuarios_academicos a ON a.id = s.alumno_id "
                                + "LEFT JOIN usuarios_academicos d ON d.id = s.docente_id")
                .setParameter("anio", anio)
                .setParameter("ahora", ahora)
                .executeUpdate();
    }
}
