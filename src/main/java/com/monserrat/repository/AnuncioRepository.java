package com.monserrat.repository;

import com.monserrat.entity.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {
    List<Anuncio> findByActivoTrueOrderByOrdenAsc();
}
