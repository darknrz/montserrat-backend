package com.monserrat.repository;

import com.monserrat.entity.HistorialAlumnoAnio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialAlumnoAnioRepository extends JpaRepository<HistorialAlumnoAnio, Long> {
    List<HistorialAlumnoAnio> findByAnioOrderByAlumnoNombreAsc(Integer anio);

    List<HistorialAlumnoAnio> findByAlumnoDniOrderByAnioDesc(String alumnoDni);

    boolean existsByAnio(Integer anio);
}
