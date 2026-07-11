package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notas_academicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaAcademica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private UsuarioAcademico alumno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private UsuarioAcademico docente;

    @Column(nullable = false, length = 50)
    private CursoAcademico curso;

    @Column(nullable = false, length = 60)
    private String periodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TipoEvaluacion tipoEvaluacion = TipoEvaluacion.EXAMEN;

    @Column(nullable = false)
    private Double valor;

    @Column(length = 300)
    private String observacion;

    @Column(name = "competencia_id", length = 50)
    private String competenciaId;

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
