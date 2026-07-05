package com.monserrat.entity;

import lombok.Getter;

@Getter
public enum CursoAcademico {
    MATEMATICA("Matematica"),
    COMUNICACION("Comunicacion"),
    CIENCIA_TECNOLOGIA("Ciencia y Tecnologia"),
    HISTORIA("Historia"),
    INGLES("Ingles"),
    ARTE_CULTURA("Arte y Cultura"),
    PERSONAL_SOCIAL("Personal Social"),
    EDUCACION_RELIGIOSA("Educacion Religiosa"),
    EDUCACION_FISICA("Educacion Fisica"),
    CASTELLANO_SEGUNDA_LENGUA("Castellano como Segunda Lengua"),
    COMPETENCIAS_TRANSVERSALES("Competencias Transversales");

    private final String nombre;

    CursoAcademico(String nombre) {
        this.nombre = nombre;
    }
}
