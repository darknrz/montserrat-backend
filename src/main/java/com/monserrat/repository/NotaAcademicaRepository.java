package com.monserrat.repository;

import com.monserrat.entity.NotaAcademica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotaAcademicaRepository extends JpaRepository<NotaAcademica, Long> {
    List<NotaAcademica> findByAlumno_DniOrderByPeriodoDescCreatedAtDesc(String dni);
    List<NotaAcademica> findByDocente_DniOrderByUpdatedAtDesc(String dni);
    List<NotaAcademica> findByAlumno_DniAndDocente_DniOrderByPeriodoDescCreatedAtDesc(String alumnoDni, String docenteDni);
}
