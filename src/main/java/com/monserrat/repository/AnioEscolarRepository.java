package com.monserrat.repository;

import com.monserrat.entity.AnioEscolar;
import com.monserrat.entity.EstadoAnioEscolar;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AnioEscolarRepository extends JpaRepository<AnioEscolar, Long> {
    Optional<AnioEscolar> findByAnio(Integer anio);

    Optional<AnioEscolar> findFirstByEstado(EstadoAnioEscolar estado);

    List<AnioEscolar> findAllByOrderByAnioDesc();

    /** Bloquea la fila del año activo: evita que dos migraciones corran a la vez. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AnioEscolar a WHERE a.estado = com.monserrat.entity.EstadoAnioEscolar.ACTIVO")
    Optional<AnioEscolar> findActivoForUpdate();
}
