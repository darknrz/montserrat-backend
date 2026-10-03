package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Foto del alumno al cierre de un año escolar. No usa claves foráneas a propósito: el histórico es un
 * archivo inmutable que debe sobrevivir aunque el alumno se elimine o cambie después.
 */
@Entity
@Table(name = "historial_alumno_anio", uniqueConstraints = {
        @UniqueConstraint(name = "uk_historial_alumno_anio", columnNames = {"alumno_id", "anio"})
}, indexes = {
        @Index(name = "idx_historial_anio", columnList = "anio"),
        @Index(name = "idx_historial_dni", columnList = "alumno_dni")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialAlumnoAnio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anio;

    @Column(name = "alumno_id", nullable = false)
    private Long alumnoId;

    @Column(name = "alumno_dni", nullable = false, length = 20)
    private String alumnoDni;

    @Column(name = "alumno_codigo", length = 30)
    private String alumnoCodigo;

    @Column(name = "alumno_nombre", nullable = false, length = 150)
    private String alumnoNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_educativo", length = 20)
    private NivelEducativo nivelEducativo;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Grado grado;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Seccion seccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoAnual resultado;

    @Enumerated(EnumType.STRING)
    @Column(name = "grado_destino", length = 50)
    private Grado gradoDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "seccion_destino", length = 50)
    private Seccion seccionDestino;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
