package com.monserrat.repository;

import com.monserrat.entity.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {
    @Query("select a from Anuncio a where a.activo = true and (a.expiresAt is null or a.expiresAt >= current_date) order by a.orden asc")
    List<Anuncio> findActiveValidOrderByOrdenAsc();
}
