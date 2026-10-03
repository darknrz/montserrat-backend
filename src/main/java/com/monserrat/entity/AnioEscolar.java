package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Año escolar del colegio. Solo uno puede estar ACTIVO a la vez (lo garantiza un índice único parcial
 * creado en {@code AnioEscolarService}). Los datos operativos (notas, asistencias, asignaciones) siempre
 * pertenecen al año activo; al cerrarlo se archivan en las tablas históricas.
 */
@Entity
@Table(name = "anios_escolares", uniqueConstraints = {
        @UniqueConstraint(name = "uk_anio_escolar_anio", columnNames = {"anio"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnioEscolar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAnioEscolar estado;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "cerrado_por", length = 100)
    private String cerradoPor;

    @Column(name = "total_promovidos")
    private Integer totalPromovidos;

    @Column(name = "total_repitentes")
    private Integer totalRepitentes;

    @Column(name = "total_egresados")
    private Integer totalEgresados;

    @Column(name = "total_retirados")
    private Integer totalRetirados;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
