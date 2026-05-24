package com.monserrat.repository;

import com.monserrat.entity.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IngresoRepository extends JpaRepository<Ingreso, Long> {
    List<Ingreso> findByActivoTrue();
    List<Ingreso> findByAnioAndActivoTrue(String anio);
    List<Ingreso> findByUniversidadSiglasAndActivoTrue(String siglas);
    List<Ingreso> findByAnioOrderByNombreAsc(String anio);
}