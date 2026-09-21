package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "matriculas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_matricula_alumno_anio", columnNames = {"alumno_id", "anio"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private UsuarioAcademico alumno;

    @Column(nullable = false)
    private Integer anio;

    private BigDecimal monto;

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
