package com.monserrat.repository;

import com.monserrat.entity.TallerCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TallerCatalogoRepository extends JpaRepository<TallerCatalogo, Long> {
    List<TallerCatalogo> findByAnioOrderByNombreAsc(Integer anio);
}
