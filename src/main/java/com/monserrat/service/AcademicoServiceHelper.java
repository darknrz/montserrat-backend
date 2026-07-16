package com.monserrat.service;

import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;

public class AcademicoServiceHelper {
    public static Grado parseGrado(String gradoStr, NivelEducativo nivelEducativo) {
        if (gradoStr == null || gradoStr.isBlank()) {
            return null;
        }

        String normalized = gradoStr.trim().toUpperCase().replace(" ", "_").replace("-", "_");

        switch (normalized) {
            case "PRIMERO":
            case "1ERO":
            case "1RO":
            case "PRIMERO_PRIMARIA":
            case "1RO_PRIMARIA":
                return Grado.PRIMERO_PRIMARIA;
            case "SEGUNDO":
            case "2DO":
            case "SEGUNDO_PRIMARIA":
            case "2DO_PRIMARIA":
                return Grado.SEGUNDO_PRIMARIA;
            case "TERCERO":
            case "3RO":
            case "TERCERO_PRIMARIA":
            case "3RO_PRIMARIA":
                return Grado.TERCERO_PRIMARIA;
            case "CUARTO":
            case "4TO":
            case "CUARTO_PRIMARIA":
            case "4TO_PRIMARIA":
                return Grado.CUARTO_PRIMARIA;
            case "QUINTO":
            case "5TO":
            case "QUINTO_PRIMARIA":
            case "5TO_PRIMARIA":
                return Grado.QUINTO_PRIMARIA;
            case "SEXTO":
            case "6TO":
            case "SEXTO_PRIMARIA":
            case "6TO_PRIMARIA":
                return Grado.SEXTO_PRIMARIA;
            case "PRIMERO_SECUNDARIA":
            case "1RO_SECUNDARIA":
                return Grado.PRIMERO_SECUNDARIA;
            case "SEGUNDO_SECUNDARIA":
            case "2DO_SECUNDARIA":
                return Grado.SEGUNDO_SECUNDARIA;
            case "TERCERO_SECUNDARIA":
            case "3RO_SECUNDARIA":
                return Grado.TERCERO_SECUNDARIA;
            case "CUARTO_SECUNDARIA":
            case "4TO_SECUNDARIA":
                return Grado.CUARTO_SECUNDARIA;
            case "QUINTO_SECUNDARIA":
            case "5TO_SECUNDARIA":
                return Grado.QUINTO_SECUNDARIA;
            default:
                return null;
        }
    }
}
