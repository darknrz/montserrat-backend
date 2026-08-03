package com.monserrat.repository;

import com.monserrat.entity.CursoAcademico;
import com.monserrat.entity.NotaAcademica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotaAcademicaRepository extends JpaRepository<NotaAcademica, Long> {
    List<NotaAcademica> findByAlumno_DniOrderByPeriodoDescCreatedAtDesc(String dni);
    List<NotaAcademica> findByDocente_DniOrderByUpdatedAtDesc(String dni);
    List<NotaAcademica> findByAlumno_DniAndDocente_DniOrderByPeriodoDescCreatedAtDesc(String alumnoDni, String docenteDni);
    long countByAlumno_Dni(String dni);
    long countByDocente_Dni(String dni);
    long deleteByAlumno_Dni(String dni);
    long deleteByDocente_Dni(String dni);

    @Query("SELECT n FROM NotaAcademica n WHERE EXISTS (" +
           "  SELECT a FROM AsignacionAcademica a WHERE a.docente.dni = :docenteDni " +
           "  AND a.alumno.id = n.alumno.id AND a.curso = n.curso AND a.activo = true" +
           ") ORDER BY n.updatedAt DESC")
    List<NotaAcademica> findNotasForDocente(@Param("docenteDni") String docenteDni);

    Optional<NotaAcademica> findByAlumno_DniAndCursoAndPeriodoAndCompetenciaId(
            String alumnoDni, CursoAcademico curso, String periodo, String competenciaId
    );
}
