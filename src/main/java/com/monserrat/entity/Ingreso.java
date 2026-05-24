package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ingresantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String universidad;

    // Siglas para el badge: UNCP, UNMSM, UNI, UPLA, etc.
    @Column(nullable = false, length = 20)
    private String universidadSiglas;

    @Column(nullable = false, length = 150)
    private String carrera;

    @Column(nullable = false, length = 4)
    private String anio;

    // Ej: "1ra Selección", "2da Selección"
    @Column(nullable = false, length = 50)
    private String tipoSeleccion;

    @Column(length = 300)
    private String fotoUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}