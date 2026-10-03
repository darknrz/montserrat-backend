package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Nota archivada al cerrar un año escolar (copia desnormalizada de {@link NotaAcademica}, sin FKs). */
@Entity
@Table(name = "notas_historicas", indexes = {
        @Index(name = "idx_nota_hist_anio", columnList = "anio"),
        @Index(name = "idx_nota_hist_alumno", columnList = "anio, alumno_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaHistorica {

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

    @Column(length = 50)
    private String curso;

    @Column(length = 60)
    private String periodo;

    @Column(name = "tipo_evaluacion", length = 30)
    private String tipoEvaluacion;

    private Double valor;

    @Column(length = 300)
    private String observacion;

    @Column(name = "competencia_id", length = 50)
    private String competenciaId;

    @Column(name = "registrado_en")
    private LocalDateTime registradoEn;

    @Column(name = "archivado_en", nullable = false)
    private LocalDateTime archivadoEn;
}
