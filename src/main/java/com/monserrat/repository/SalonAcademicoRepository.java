package com.monserrat.repository;

import com.monserrat.entity.SalonAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalonAcademicoRepository extends JpaRepository<SalonAcademico, Long> {
    List<SalonAcademico> findAllByOrderByOrdenAscIdAsc();
}
