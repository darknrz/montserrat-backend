package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pensiones_mensuales", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pension_alumno_anio_mes", columnNames = {"alumno_id", "anio", "mes"})
}, indexes = {
        @Index(name = "idx_pensiones_anio_mes", columnList = "anio,mes")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PensionMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private UsuarioAcademico alumno;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean pagada = false;

    @Column(length = 200)
    private String observacion;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }
}
