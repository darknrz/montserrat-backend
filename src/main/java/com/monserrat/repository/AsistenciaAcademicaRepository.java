package com.monserrat.repository;

import com.monserrat.entity.AsistenciaAcademica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenciaAcademicaRepository extends JpaRepository<AsistenciaAcademica, Long> {
    List<AsistenciaAcademica> findByAlumno_DniOrderByFechaDesc(String dni);
    List<AsistenciaAcademica> findByDocente_DniOrderByFechaDesc(String dni);
    List<AsistenciaAcademica> findByAlumno_DniAndDocente_DniOrderByFechaDesc(String alumnoDni, String docenteDni);
    long countByAlumno_Dni(String dni);
    long countByDocente_Dni(String dni);
    long deleteByAlumno_Dni(String dni);
    long deleteByDocente_Dni(String dni);
}
