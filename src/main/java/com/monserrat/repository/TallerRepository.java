package com.monserrat.repository;

import com.monserrat.entity.Taller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TallerRepository extends JpaRepository<Taller, Long> {
    List<Taller> findByAnioOrderByAlumno_NombreAsc(Integer anio);
    List<Taller> findAllByOrderByAlumno_NombreAsc();
    List<Taller> findByAlumno_DniOrderByAnioDesc(String alumnoDni);
    List<Taller> findByCatalogo_Id(Long catalogoId);
    java.util.Optional<Taller> findByAlumno_DniAndCatalogo_Id(String dni, Long catalogoId);
    List<Taller> findByCatalogoIsNullAndAnio(Integer anio);
    long countByAlumno_Dni(String dni);
    long deleteByAlumno_Dni(String dni);
}
