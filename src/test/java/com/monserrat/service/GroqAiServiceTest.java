package com.monserrat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroqAiServiceTest {

    private final GroqAiService groqAiService = new GroqAiService(new ObjectMapper());

    @Test
    void fallbackUsesDetectedIntentForMatriculaTypos() {
        String answer = groqAiService.answer("mnatricula", "", "MATRICULA", null);

        assertThat(answer).containsIgnoringCase("matricula");
        assertThat(answer).contains("monserratcomplejoeducativo@gmail.com");
    }

    @Test
    void fallbackAnswersUniformQuestions() {
        String answer = groqAiService.answer("como es el uniforme", "", "UNIFORME", null);

        assertThat(answer).containsIgnoringCase("uniforme");
        assertThat(answer).contains("monserratcomplejoeducativo@gmail.com");
    }

    @Test
    void answersIngresantesListWithStudentBullets() {
        String answer = groqAiService.answer("dame la lista de ingresantes", ingresantesContext(), "INGRESANTES", null);

        assertThat(answer).contains("Lista de ingresantes registrados:");
        assertThat(answer).contains("2025:");
        assertThat(answer).contains("- Ana Torres - UNCP - Medicina (1ra Seleccion)");
        assertThat(answer).contains("- Luis Ramos - UNI - Ingenieria Civil (1ra Seleccion)");
    }

    @Test
    void answersIngresantesByYearWithStudentDetails() {
        String answer = groqAiService.answer("ingresantes 2024", ingresantesContext(), "INGRESANTES", null);

        assertThat(answer).contains("Ingresantes registrados en 2024:");
        assertThat(answer).contains("- Maria Quispe - UNMSM - Derecho (1ra Seleccion)");
        assertThat(answer).doesNotContain("Ana Torres");
    }

    private static String ingresantesContext() {
        return """
                INGRESANTES
                - Ana Torres | UNCP | Medicina | 2025 | 1ra Seleccion
                - Luis Ramos | UNI | Ingenieria Civil | 2025 | 1ra Seleccion
                - Maria Quispe | UNMSM | Derecho | 2024 | 1ra Seleccion
                """;
    }
}
