package com.monserrat.repository;

import com.monserrat.entity.PeriodoBimestre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PeriodoBimestreRepository extends JpaRepository<PeriodoBimestre, Long> {
    List<PeriodoBimestre> findByAnio(Integer anio);

    Optional<PeriodoBimestre> findByAnioAndNumeroBimestre(Integer anio, Integer numeroBimestre);

    @Query("SELECT p FROM PeriodoBimestre p WHERE p.anio = :anio ORDER BY p.numeroBimestre ASC")
    List<PeriodoBimestre> findByAnioOrderedByBimestre(@Param("anio") Integer anio);

    @Query("SELECT p FROM PeriodoBimestre p WHERE p.anio = :anio AND p.fechaInicio <= :fecha AND :fecha <= p.fechaFin")
    Optional<PeriodoBimestre> findByAnioAndFecha(@Param("anio") Integer anio, @Param("fecha") LocalDate fecha);

    List<PeriodoBimestre> findByAnioOrderByNumeroBimestreAsc(Integer anio);
}
