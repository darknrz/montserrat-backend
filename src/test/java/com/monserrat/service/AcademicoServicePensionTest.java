package com.monserrat.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcademicoServicePensionTest {

    @Test
    void shouldActivateMonthsFromRegistrationYearAndMonth() {
        LocalDateTime fechaInicio = LocalDateTime.of(2024, 5, 10, 0, 0);

        assertTrue(AcademicoService.esMesPensionActiva(fechaInicio, 2024, 5, LocalDate.of(2024, 8, 15)));
        assertFalse(AcademicoService.esMesPensionActiva(fechaInicio, 2024, 4, LocalDate.of(2024, 8, 15)));
        assertFalse(AcademicoService.esMesPensionActiva(fechaInicio, 2025, 1, LocalDate.of(2024, 8, 15)));
    }

    @Test
    void shouldBlockFutureMonthsBeyondReferenceDate() {
        LocalDateTime fechaInicio = LocalDateTime.of(2024, 1, 1, 0, 0);

        assertTrue(AcademicoService.esMesPensionActiva(fechaInicio, 2024, 1, LocalDate.of(2024, 2, 1)));
        assertFalse(AcademicoService.esMesPensionActiva(fechaInicio, 2024, 3, LocalDate.of(2024, 2, 1)));
    }
}
