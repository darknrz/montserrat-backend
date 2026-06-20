package com.monserrat.entity;

import lombok.Getter;

@Getter
public enum CursoAcademico {
    MATEMATICA("Matematica"),
    COMUNICACION("Comunicacion"),
    CIENCIA_TECNOLOGIA("Ciencia y Tecnologia"),
    HISTORIA("Historia"),
    INGLES("Ingles");

    private final String nombre;

    CursoAcademico(String nombre) {
        this.nombre = nombre;
    }
}
