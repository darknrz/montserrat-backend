package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "institution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 300)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(nullable = false, length = 100)
    private String distrito;

    @Column(nullable = false, length = 4)
    private String anioFundacion;

    @Column(nullable = false, length = 100)
    private String telefono;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String niveles;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mision;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String vision;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 300)
    private String logoUrl;

    // Horario de atención
    @Column(length = 100)
    private String horarioAtencion;
}