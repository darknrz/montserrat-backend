package com.monserrat.repository;

import com.monserrat.entity.Taller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TallerRepository extends JpaRepository<Taller, Long> {
    List<Taller> findByAnioOrderByAlumno_NombreAsc(Integer anio);
    List<Taller> findAllByOrderByAlumno_NombreAsc();
    List<Taller> findByAlumno_DniOrderByAnioDesc(String alumnoDni);
    long countByAlumno_Dni(String dni);
    long deleteByAlumno_Dni(String dni);
}
