package com.monserrat.repository;

import com.monserrat.entity.AsignacionAcademica;
import com.monserrat.entity.CursoAcademico;
import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface AsignacionAcademicaRepository extends JpaRepository<AsignacionAcademica, Long> {
    @EntityGraph(attributePaths = {"docente", "alumno"})
    List<AsignacionAcademica> findByDocente_DniAndActivoTrue(String dni);

    @EntityGraph(attributePaths = {"docente", "alumno"})
    List<AsignacionAcademica> findByAlumno_DniAndActivoTrue(String dni);

    @EntityGraph(attributePaths = {"docente", "alumno"})
    List<AsignacionAcademica> findByActivoTrue();

    @EntityGraph(attributePaths = {"docente", "alumno"})
    List<AsignacionAcademica> findByNivelEducativoAndGradoAndSeccionAndActivoTrue(
            NivelEducativo nivelEducativo,
            Grado grado,
            Seccion seccion);

    boolean existsByDocente_DniAndAlumno_DniAndCursoAndGradoAndSeccionAndActivoTrue(
            String docenteDni,
            String alumnoDni,
            CursoAcademico curso,
            com.monserrat.entity.Grado grado,
            com.monserrat.entity.Seccion seccion);

    @EntityGraph(attributePaths = {"docente", "alumno"})
    Optional<AsignacionAcademica> findByIdAndActivoTrue(Long id);
}
