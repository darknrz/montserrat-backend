package com.monserrat.repository;

import com.monserrat.entity.AsistenciaAcademica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaAcademicaRepository extends JpaRepository<AsistenciaAcademica, Long> {
    List<AsistenciaAcademica> findByAlumno_DniOrderByFechaDesc(String dni);
    List<AsistenciaAcademica> findByDocente_DniOrderByFechaDesc(String dni);
    List<AsistenciaAcademica> findByAlumno_DniAndDocente_DniOrderByFechaDesc(String alumnoDni, String docenteDni);
    long countByAlumno_Dni(String dni);
    long countByDocente_Dni(String dni);
    long deleteByAlumno_Dni(String dni);
    long deleteByDocente_Dni(String dni);

    @Query("SELECT ast FROM AsistenciaAcademica ast WHERE EXISTS (" +
           "  SELECT a FROM AsignacionAcademica a WHERE a.docente.dni = :docenteDni " +
           "  AND a.alumno.id = ast.alumno.id AND a.activo = true" +
           ") ORDER BY ast.fecha DESC")
    List<AsistenciaAcademica> findAsistenciasForDocente(@Param("docenteDni") String docenteDni);

    Optional<AsistenciaAcademica> findByAlumno_DniAndFecha(String alumnoDni, LocalDate fecha);
}
