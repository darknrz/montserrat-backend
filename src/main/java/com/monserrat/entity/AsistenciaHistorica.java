package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Asistencia archivada al cerrar un año escolar (copia desnormalizada, sin FKs). */
@Entity
@Table(name = "asistencias_historicas", indexes = {
        @Index(name = "idx_asist_hist_anio", columnList = "anio"),
        @Index(name = "idx_asist_hist_alumno", columnList = "anio, alumno_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaHistorica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anio;

    @Column(name = "alumno_id", nullable = false)
    private Long alumnoId;

    @Column(name = "alumno_dni", length = 20)
    private String alumnoDni;

    @Column(name = "alumno_nombre", length = 150)
    private String alumnoNombre;

    @Column(name = "docente_id")
    private Long docenteId;

    @Column(name = "docente_nombre", length = 150)
    private String docenteNombre;

    @Column(length = 50)
    private String grado;

    @Column(length = 50)
    private String seccion;

    private LocalDate fecha;

    @Column(length = 20)
    private String estado;

    @Column(length = 300)
    private String observacion;

    @Column(name = "archivado_en", nullable = false)
    private LocalDateTime archivadoEn;
}
