package com.monserrat.repository;

import com.monserrat.entity.NotaHistorica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotaHistoricaRepository extends JpaRepository<NotaHistorica, Long> {
    List<NotaHistorica> findByAnioAndAlumnoDniOrderByPeriodoAscCursoAsc(Integer anio, String alumnoDni);

    long countByAnio(Integer anio);
}
