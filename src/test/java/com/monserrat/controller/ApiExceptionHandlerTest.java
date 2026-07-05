package com.monserrat.controller;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldMapTooLongValueToFriendlyMessage() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "ERROR: value too long for type character varying(120)"
        );

        ResponseEntity<Map<String, Object>> response = handler.handleDataIntegrity(ex);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("El texto supera el máximo permitido de 500 caracteres", response.getBody().get("message"));
    }

    @Test
    void shouldHandleIllegalArgumentAsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("El texto no puede superar 500 caracteres");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("El texto no puede superar 500 caracteres", response.getBody().get("message"));
    }

    @Test
    void shouldMapUnreadableEnumValueToFriendlyMessage() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "Cannot coerce empty String (\"\") to com.monserrat.entity.Grado value"
        );

        ResponseEntity<Map<String, Object>> response = handler.handleMessageNotReadable(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("El valor enviado para un campo enum no es valido", response.getBody().get("message"));
    }
}
