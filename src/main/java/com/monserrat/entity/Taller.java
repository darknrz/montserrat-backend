package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "talleres")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Taller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private UsuarioAcademico alumno;

    @Column(nullable = false)
    private Integer anio;

    // Detalle del taller (ej. "Taller de Robotica", "Danza")
    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    @Builder.Default
    private Boolean pagada = false;

    @Column(length = 200)
    private String observacion;

    @Column(nullable = false)
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
