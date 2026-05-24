package com.monserrat.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ChatbotGuardServiceTest {

    private final ChatbotGuardService guardService = new ChatbotGuardService();

    @Test
    void detectsNoiseAndReturnsNotUnderstoodResponse() {
        ChatbotMessageAnalysis analysis = guardService.analyze("asdfgh ???");

        assertThat(analysis.intent()).isEqualTo("NO_ENTENDIDO");
        assertThat(analysis.hasDirectResponse()).isTrue();
        assertThat(analysis.directResponse()).contains("No te entendi bien");
    }

    @Test
    void detectsLongKeyboardNoise() {
        ChatbotMessageAnalysis analysis = guardService.analyze("asdfgh qwerty zxcvbn");

        assertThat(analysis.intent()).isEqualTo("NO_ENTENDIDO");
        assertThat(analysis.hasDirectResponse()).isTrue();
    }

    @Test
    void detectsOutOfScopeQuestions() {
        ChatbotMessageAnalysis analysis = guardService.analyze("quien gano el partido de futbol");

        assertThat(analysis.intent()).isEqualTo("FUERA_DE_TEMA");
        assertThat(analysis.hasDirectResponse()).isTrue();
        assertThat(analysis.directResponse()).contains("Solo puedo ayudarte");
    }

    @Test
    void ToleratesTyposInInstitutionalQuestions() {
        ChatbotMessageAnalysis analysis = guardService.analyze("quiero informacion de matrikula");

        assertThat(analysis.intent()).isEqualTo("MATRICULA");
        assertThat(analysis.hasDirectResponse()).isFalse();
        assertThat(analysis.confidence()).isGreaterThanOrEqualTo(BigDecimal.valueOf(0.60));
    }

    @Test
    void ClassifiesInstitutionalTopics() {
        ChatbotMessageAnalysis analysis = guardService.analyze("cual es el horario de atencion");

        assertThat(analysis.intent()).isEqualTo("HORARIO");
        assertThat(analysis.hasDirectResponse()).isFalse();
    }

    @Test
    void answersSocialQuestionsDirectly() {
        ChatbotMessageAnalysis analysis = guardService.analyze("como estas");

        assertThat(analysis.intent()).isEqualTo("CONVERSACION");
        assertThat(analysis.hasDirectResponse()).isTrue();
        assertThat(analysis.directResponse()).contains("**Asistente Monserrat**");
    }

    @Test
    void extractsVisitorName() {
        ChatbotMessageAnalysis analysis = guardService.analyze("me llamo elvis");

        assertThat(analysis.intent()).isEqualTo("PRESENTACION");
        assertThat(analysis.hasVisitorName()).isTrue();
        assertThat(analysis.visitorName()).isEqualTo("Elvis");
    }

    @Test
    void classifiesUniformQuestions() {
        ChatbotMessageAnalysis analysis = guardService.analyze("como es el uniforme");

        assertThat(analysis.intent()).isEqualTo("UNIFORME");
        assertThat(analysis.hasDirectResponse()).isFalse();
    }

    @Test
    void detectsYearOnlyFollowUps() {
        ChatbotMessageAnalysis analysis = guardService.analyze("en el ano 2025");

        assertThat(analysis.intent()).isEqualTo("SEGUIMIENTO");
        assertThat(analysis.hasDirectResponse()).isFalse();
    }

    @Test
    void answersCapabilitiesEvenWithTypos() {
        ChatbotMessageAnalysis analysis = guardService.analyze("que pudes hacer");

        assertThat(analysis.intent()).isEqualTo("AYUDA");
        assertThat(analysis.hasDirectResponse()).isTrue();
        assertThat(analysis.directResponse()).contains("Puedo ayudarte");
        assertThat(analysis.directResponse()).contains("ingresantes");
    }
}
