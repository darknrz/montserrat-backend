package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "usuarios_academicos", indexes = {
        @Index(name = "idx_usuarios_academicos_dni", columnList = "dni", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(unique = true, length = 30)
    private String codigo;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 100)
    private String nombres;

    @Column(length = 120)
    private String apellidos;

    @Column(length = 120)
    private String correo;

    @Column(length = 250)
    private String direccion;

    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean debeCambiarContrasena = true;

    @Column(length = 30)
    private String telefono;

    @Column(length = 300)
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NivelEducativo nivelEducativo;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Grado grado;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Seccion seccion;

    @Column(length = 120)
    private String materia;

    @Column(length = 120)
    private String especialidad;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private EstadoMatricula estadoMatricula = EstadoMatricula.MATRICULADO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean pensionPagada = false;

    @Column(length = 200)
    private String pensionObservacion;
}
