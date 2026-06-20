package com.monserrat.entity;

import lombok.Getter;

@Getter
public enum Grado {
    PRIMERO_PRIMARIA("1ro Primaria"),
    SEGUNDO_PRIMARIA("2do Primaria"),
    TERCERO_PRIMARIA("3ro Primaria"),
    CUARTO_PRIMARIA("4to Primaria"),
    QUINTO_PRIMARIA("5to Primaria"),
    SEXTO_PRIMARIA("6to Primaria"),
    PRIMERO_SECUNDARIA("1ro Secundaria"),
    SEGUNDO_SECUNDARIA("2do Secundaria"),
    TERCERO_SECUNDARIA("3ro Secundaria"),
    CUARTO_SECUNDARIA("4to Secundaria"),
    QUINTO_SECUNDARIA("5to Secundaria");

    private final String nombre;

    Grado(String nombre) {
        this.nombre = nombre;
    }
}