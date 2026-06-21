package com.monserrat.repository;

import com.monserrat.entity.CatalogoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogoAcademicoRepository extends JpaRepository<CatalogoAcademico, Long> {
    List<CatalogoAcademico> findAllByOrderByOrdenAscIdAsc();
}
