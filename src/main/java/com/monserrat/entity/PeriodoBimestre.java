package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "periodos_bimestres", uniqueConstraints = {
        @UniqueConstraint(name = "uk_periodo_anio_numero", columnNames = {"anio", "numero_bimestre"})
}, indexes = {
        @Index(name = "idx_periodo_anio", columnList = "anio")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoBimestre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false, name = "numero_bimestre")
    private Integer numeroBimestre; // 1, 2, 3, 4

    @Column(nullable = false, name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(nullable = false, name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreated() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
}
