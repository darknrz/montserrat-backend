package com.monserrat.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CursoAcademicoConverter implements AttributeConverter<CursoAcademico, String> {
    @Override
    public String convertToDatabaseColumn(CursoAcademico attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public CursoAcademico convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        String normalized = dbData.trim().toUpperCase()
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace(" Y ", "_")
                .replace(" ", "_");
        for (CursoAcademico curso : CursoAcademico.values()) {
            if (curso.name().equals(normalized) || curso.getNombre().equalsIgnoreCase(dbData.trim())) {
                return curso;
            }
        }
        throw new IllegalArgumentException("Curso academico no soportado: " + dbData);
    }
}
