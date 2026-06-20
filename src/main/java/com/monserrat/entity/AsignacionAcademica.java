package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones_academicas", indexes = {
        @Index(name = "idx_asig_docente", columnList = "docente_id"),
        @Index(name = "idx_asig_alumno", columnList = "alumno_id"),
        @Index(name = "idx_asig_curso", columnList = "curso")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionAcademica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private UsuarioAcademico docente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private UsuarioAcademico alumno;

    @Column(nullable = false, length = 50)
    private CursoAcademico curso;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NivelEducativo nivelEducativo;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Grado grado;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Seccion seccion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

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
