package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "salones_academicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalonAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String nivel;

    @Column(nullable = false, length = 80)
    private String grado;

    @Column(nullable = false, length = 20)
    private String seccion;

    @Column(nullable = false, length = 30)
    private String aula;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;
}
