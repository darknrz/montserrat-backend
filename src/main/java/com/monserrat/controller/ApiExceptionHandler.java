package com.monserrat.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(body(ex.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> {
                    String message = fieldError.getDefaultMessage();
                    if (message != null && !message.isBlank()) {
                        return message;
                    }
                    return capitalize(fieldError.getField()) + " es invalido";
                })
                .distinct()
                .toList();
        String message = errors.isEmpty() ? "Los datos enviados no son validos" : String.join(". ", errors);
        return ResponseEntity.badRequest().body(body(message, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Los datos enviados no son validos");
        return ResponseEntity.badRequest().body(body(message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = mapConstraintMessage(ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body("Ocurrio un error inesperado"));
    }

    private Map<String, Object> body(String message) {
        return body(message, null);
    }

    private Map<String, Object> body(String message, Object details) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message == null || message.isBlank() ? "Ocurrio un error inesperado" : message);
        if (details != null) {
            response.put("details", details);
        }
        return response;
    }

    private String mapConstraintMessage(Throwable ex) {
        String text = rootMessage(ex);
        String normalized = text == null ? "" : text.toLowerCase();

        if (normalized.contains("dni")) {
            return "El DNI ya esta registrado";
        }
        if (normalized.contains("correo") || normalized.contains("email")) {
            return "El correo ya esta registrado";
        }
        if (normalized.contains("codigo")) {
            return "El codigo ya esta registrado";
        }
        return "No se pudo guardar el registro porque ya existe un dato duplicado";
    }

    private String rootMessage(Throwable ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getMessage();
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "El campo";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
