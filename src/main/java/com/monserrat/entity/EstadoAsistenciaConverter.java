package com.monserrat.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoAsistenciaConverter implements AttributeConverter<EstadoAsistencia, String> {
    @Override
    public String convertToDatabaseColumn(EstadoAsistencia attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public EstadoAsistencia convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return EstadoAsistencia.valueOf(dbData.trim().toUpperCase());
    }
}
