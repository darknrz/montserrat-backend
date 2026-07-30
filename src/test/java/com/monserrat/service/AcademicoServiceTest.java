package com.monserrat.service;

import com.monserrat.entity.AsignacionAcademica;
import com.monserrat.entity.CursoAcademico;
import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.Seccion;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.repository.AsignacionAcademicaRepository;
import com.monserrat.repository.AsistenciaAcademicaRepository;
import com.monserrat.repository.CatalogoAcademicoRepository;
import com.monserrat.repository.NotaAcademicaRepository;
import com.monserrat.repository.PensionMensualRepository;
import com.monserrat.repository.PeriodoBimestreRepository;
import com.monserrat.repository.UsuarioAcademicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicoServiceTest {

    @Mock
    private UsuarioAcademicoRepository usuarioRepository;

    @Mock
    private AsignacionAcademicaRepository asignacionRepository;

    @Mock
    private AsistenciaAcademicaRepository asistenciaRepository;

    @Mock
    private NotaAcademicaRepository notaRepository;

    @Mock
    private PensionMensualRepository pensionMensualRepository;

    @Mock
    private PeriodoBimestreRepository periodoBimestreRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CatalogoAcademicoRepository catalogoRepository;

    @InjectMocks
    private AcademicoService academicoService;

    @Test
    void crearAsignacionesPorDocentesDebeCrearUnaAsignacionPorDocenteCuandoHayVarios() {
        UsuarioAcademico alumno = UsuarioAcademico.builder()
                .dni("10000000")
                .rol(RolUsuario.ALUMNO)
                .nivelEducativo(NivelEducativo.SECUNDARIA)
                .grado(Grado.PRIMERO_SECUNDARIA)
                .seccion(Seccion.A)
                .activo(true)
                .build();

        UsuarioAcademico docenteUno = UsuarioAcademico.builder()
                .dni("20000001")
                .rol(RolUsuario.DOCENTE)
                .activo(true)
                .build();

        UsuarioAcademico docenteDos = UsuarioAcademico.builder()
                .dni("20000002")
                .rol(RolUsuario.DOCENTE)
                .activo(true)
                .build();

        when(usuarioRepository.findByDni("20000001")).thenReturn(Optional.of(docenteUno));
        when(usuarioRepository.findByDni("20000002")).thenReturn(Optional.of(docenteDos));
        when(asignacionRepository.existsByDocente_DniAndAlumno_DniAndCursoAndGradoAndSeccionAndActivoTrue(
                "20000001", "10000000", CursoAcademico.MATEMATICA, alumno.getGrado(), alumno.getSeccion()))
                .thenReturn(false);
        when(asignacionRepository.existsByDocente_DniAndAlumno_DniAndCursoAndGradoAndSeccionAndActivoTrue(
                "20000002", "10000000", CursoAcademico.MATEMATICA, alumno.getGrado(), alumno.getSeccion()))
                .thenReturn(false);
        when(asignacionRepository.save(any(AsignacionAcademica.class))).thenAnswer(invocation -> invocation.getArgument(0));

        academicoService.crearAsignacionesPorDocentes(alumno, CursoAcademico.MATEMATICA, "20000001,20000002");

        verify(asignacionRepository, times(2)).save(any(AsignacionAcademica.class));
    }
}
