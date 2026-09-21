package com.monserrat.repository;

import com.monserrat.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
    List<Matricula> findByAnio(Integer anio);
    Optional<Matricula> findByAlumno_DniAndAnio(String alumnoDni, Integer anio);
    long countByAlumno_Dni(String dni);
    long deleteByAlumno_Dni(String dni);
}
