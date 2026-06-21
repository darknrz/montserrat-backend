package com.monserrat.repository;

import com.monserrat.entity.PensionMensual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PensionMensualRepository extends JpaRepository<PensionMensual, Long> {
    List<PensionMensual> findByAnio(Integer anio);
    Optional<PensionMensual> findByAlumno_DniAndAnioAndMes(String alumnoDni, Integer anio, Integer mes);
}
