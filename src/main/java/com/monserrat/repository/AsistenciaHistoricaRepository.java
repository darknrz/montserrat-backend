package com.monserrat.repository;

import com.monserrat.entity.AsistenciaHistorica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenciaHistoricaRepository extends JpaRepository<AsistenciaHistorica, Long> {
    List<AsistenciaHistorica> findByAnioAndAlumnoDniOrderByFechaDesc(Integer anio, String alumnoDni);

    long countByAnio(Integer anio);
}
