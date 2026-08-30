package com.monserrat.repository;

import com.monserrat.entity.CatalogoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatalogoAcademicoRepository extends JpaRepository<CatalogoAcademico, Long> {
    List<CatalogoAcademico> findAllByOrderByOrdenAscIdAsc();
    Optional<CatalogoAcademico> findByTipoAndNivelAndCodigo(String tipo, String nivel, String codigo);
}
