package com.monserrat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class GroqAiService {

    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(20\\d{2})\\b");

    private final ObjectMapper objectMapper;

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.model:llama-3.1-8b-instant}")
    private String model;

    public String answer(String userMessage, String context) {
        return answer(userMessage, context, null, null);
    }

    public String answer(String userMessage, String context, String intent, String visitorName) {
        return answerInternal(userMessage, context, intent, visitorName, "", "");
    }

    public String answer(
            String userMessage,
            String context,
            String intent,
            String visitorName,
            String conversationHistory,
            String previousBotAnswer
    ) {
        return answerInternal(userMessage, context, intent, visitorName, conversationHistory, previousBotAnswer);
    }

    private String answerInternal(
            String userMessage,
            String context,
            String intent,
            String visitorName,
            String conversationHistory,
            String previousBotAnswer
    ) {
        if (isConfigured()) {
            try {
                return callGroq(systemPrompt(context, visitorName, conversationHistory, previousBotAnswer, intent), userMessage, 0.55);
            } catch (Exception ignored) {
                return fallbackAnswer(userMessage, context, intent);
            }
        }

        String directAnswer = directAnswer(userMessage, context);
        if (directAnswer != null) {
            return directAnswer;
        }

        return fallbackAnswer(userMessage, context, intent);
    }

    /**
     * Llamada de proposito general al modelo: le pasas tu propio system prompt y
     * user prompt ya armados (por ejemplo con datos verificados de un alumno) y
     * te devuelve el texto generado tal cual.
     *
     * A diferencia de answer(...), esta NO tiene un fallback interno: si Groq
     * falla (sin API key, error HTTP, timeout, etc.) lanza una excepcion para
     * que quien la llama decida su propio fallback (por ejemplo, el que llama
     * puede mostrar una respuesta con formato fijo en vez de esta version natural).
     */
    public String generate(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            throw new IllegalStateException("Groq API key no configurada");
        }
        return callGroq(systemPrompt, userPrompt, 0.4);
    }

    public boolean isConfigured() {
        return !resolveApiKey().isBlank();
    }

    private String callGroq(String systemPrompt, String userPrompt, double temperature) {
        try {
            Map<String, Object> payload = Map.of(
                    "model", resolveModel(),
                    "temperature", temperature,
                    "max_tokens", 450,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(25))
                    .header("Authorization", "Bearer " + resolveApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Groq respondio con status " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Error llamando a Groq", e);
        }
    }

    private String resolveApiKey() {
        return resolveConfigValue(apiKey, "GROQ_API_KEY");
    }

    private String resolveModel() {
        String resolvedModel = resolveConfigValue(model, "GROQ_MODEL");
        return resolvedModel.isBlank() ? "llama-3.1-8b-instant" : resolvedModel;
    }

    private String resolveConfigValue(String configuredValue, String envKey) {
        if (configuredValue != null && !configuredValue.isBlank()) {
            return configuredValue;
        }

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return readDotenvValue(envKey).orElse("");
    }

    private Optional<String> readDotenvValue(String key) {
        if (isRunningTests()) {
            return Optional.empty();
        }

        for (Path dotenvPath : List.of(Path.of(".env"), Path.of("..", ".env"))) {
            if (!Files.isRegularFile(dotenvPath)) {
                continue;
            }

            try {
                for (String line : Files.readAllLines(dotenvPath)) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("#") || !trimmed.startsWith(key + "=")) {
                        continue;
                    }

                    return Optional.of(trimmed.substring((key + "=").length()).trim());
                }
            } catch (IOException ignored) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private boolean isRunningTests() {
        return System.getProperty("surefire.test.class.path") != null
                || System.getProperty("java.class.path", "").contains("target/test-classes");
    }

    private String systemPrompt(String context, String visitorName, String conversationHistory, String previousBotAnswer, String intent) {
        return """
                Eres el Asistente Monserrat de la I.E.P. Nuestra Senora de Monserrat de Huancayo.
                Conversas en espanol natural, cercano y profesional, como una persona de admision del colegio.

                Reglas:
                - Usa solamente la informacion institucional incluida en el contexto.
                - No inventes costos, vacantes, requisitos, telefonos, fechas ni datos no incluidos.
                - Si preguntan por pensiones, costos o informacion no disponible, deriva al correo institucional.
                - Si detectas interes de matricula, pide nombre y telefono de contacto de forma amable.
                - Si preguntan por ubicacion, horario o correo, responde directo, sin sonar robotico.
                - Si preguntan por ingresantes de un ano, menciona algunos nombres del ano solicitado si estan en el contexto.
                No respondas preguntas externas como politica, deportes, recetas, programacion, clima, entretenimiento o finanzas.
                - Responde breve: 2 a 5 lineas, salvo que pidan una lista.
                - No repitas literalmente tu respuesta anterior. Si el usuario pregunta lo mismo, confirma de otra forma,
                  resume distinto o agrega un matiz util sin inventar.
                - Evita empezar siempre con "Hola" o cerrar siempre igual.

                Intencion detectada: %s

                Conversacion reciente:
                %s

                Ultima respuesta del bot que debes evitar repetir literalmente:
                %s

                CONTEXTO:
                """.formatted(
                        intent == null || intent.isBlank() ? "GENERAL" : intent,
                        conversationHistory == null || conversationHistory.isBlank() ? "(sin historial previo)" : conversationHistory,
                        previousBotAnswer == null || previousBotAnswer.isBlank() ? "(ninguna)" : previousBotAnswer
                ) + (visitorName == null || visitorName.isBlank() ? "" : "Visitante: " + visitorName + "\n") + context;
    }

    private String directAnswer(String userMessage, String context) {
        String text = normalize(userMessage);

        if (text.contains("matricula") || text.contains("vacante")) {
            return "**Matricula:** escribenos a **monserratcomplejoeducativo@gmail.com** o visitanos en **Jr. Cajamarca #563, Huancayo**.";
        }
        if (text.contains("pension") || text.contains("costo") || text.contains("precio")) {
            return "**Pensiones o costos:** comunicate directamente al correo **monserratcomplejoeducativo@gmail.com**.";
        }
        if (text.contains("uniforme") || text.contains("buzo") || text.contains("vestimenta")) {
            return "**Uniforme:** para informacion actualizada, comunicate con la institucion al correo **monserratcomplejoeducativo@gmail.com** o visitanos en **Jr. Cajamarca #563, Huancayo**.";
        }
        if (text.contains("horario")) {
            return extractSingleLine(context, "Horario", "**Horario de atencion:** Lun-Vie 7:30am-5:00pm.", "**Horario de atencion:** %s.");
        }
        if (text.contains("direccion") || text.contains("ubicacion") || text.contains("donde queda") || text.contains("donde esta")) {
            return extractSingleLine(context, "Direccion", "**Ubicacion:** Jr. Cajamarca #563, Huancayo.\n**Contacto:** monserratcomplejoeducativo@gmail.com.", "**Ubicacion:** %s.\n**Contacto:** monserratcomplejoeducativo@gmail.com.");
        }
        if (text.contains("correo") || text.contains("email")) {
            return extractSingleLine(context, "Correo", "**Correo:** monserratcomplejoeducativo@gmail.com.", "**Correo:** %s.");
        }
        if (text.contains("ingresaron") || text.contains("ingresante") || text.contains("alumnos") || text.contains("universidad")) {
            String byYear = answerIngresantesByYear(text, context);
            if (byYear != null) {
                return byYear;
            }
            return answerIngresantesList(context);
        }
        return null;
    }

    private String fallbackAnswer(String userMessage, String context, String intent) {
        String text = normalize(userMessage);
        if (text.contains("matricula") || "MATRICULA".equals(intent)) {
            return "**Matricula:** escribenos a **monserratcomplejoeducativo@gmail.com** o visitanos en **Jr. Cajamarca #563, Huancayo**.";
        }
        if (text.contains("horario") || "HORARIO".equals(intent)) {
            return extractSingleLine(context, "Horario", "**Horario de atencion:** Lun-Vie 7:30am-5:00pm.", "**Horario de atencion:** %s.");
        }
        if (text.contains("direccion") || text.contains("ubicacion") || text.contains("donde queda") || text.contains("donde esta") || "UBICACION".equals(intent)) {
            return extractSingleLine(context, "Direccion", "**Ubicacion:** Jr. Cajamarca #563, Huancayo.\n**Contacto:** monserratcomplejoeducativo@gmail.com.", "**Ubicacion:** %s.\n**Contacto:** monserratcomplejoeducativo@gmail.com.");
        }
        if (text.contains("ingresante") || text.contains("universidad") || text.contains("alumnos") || text.contains("ingresaron") || "INGRESANTES".equals(intent)) {
            String byYear = answerIngresantesByYear(text, context);
            if (byYear != null) {
                return byYear;
            }
            return answerIngresantesList(context);
        }
        if (text.contains("pension") || text.contains("costo") || text.contains("precio") || "COSTOS".equals(intent)) {
            return "**Pensiones o costos:** comunicate directamente al correo **monserratcomplejoeducativo@gmail.com**.";
        }
        if (text.contains("uniforme") || text.contains("buzo") || text.contains("vestimenta") || "UNIFORME".equals(intent)) {
            return "**Uniforme:** para informacion actualizada, comunicate con la institucion al correo **monserratcomplejoeducativo@gmail.com** o visitanos en **Jr. Cajamarca #563, Huancayo**.";
        }
        return "Puedo ayudarte con matricula, horarios, ubicacion, niveles, ingresantes, videos y redes institucionales. Para una consulta especifica, escribenos a monserratcomplejoeducativo@gmail.com.";
    }

    private String answerIngresantesByYear(String normalizedMessage, String context) {
        Matcher matcher = YEAR_PATTERN.matcher(normalizedMessage);
        if (!matcher.find()) {
            return null;
        }

        String year = matcher.group(1);
        List<IngresanteInfo> ingresantes = parseIngresantes(context).stream()
                .filter(ingresante -> year.equals(ingresante.anio()))
                .limit(5)
                .toList();

        if (ingresantes.isEmpty()) {
            return "No encontre ingresantes registrados para " + year + " en este momento.";
        }

        return "**Ingresantes registrados en " + year + ":**\n" + formatIngresantes(ingresantes);
    }

    private String answerIngresantesList(String context) {
        List<IngresanteInfo> ingresantes = parseIngresantes(context);
        if (ingresantes.isEmpty()) {
            return "No encontre ingresantes registrados en este momento.";
        }

        String grouped = ingresantes.stream()
                .sorted(Comparator.comparing(IngresanteInfo::anio).reversed().thenComparing(IngresanteInfo::universidad))
                .collect(Collectors.groupingBy(
                        IngresanteInfo::anio,
                        java.util.LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":\n" + formatIngresantes(entry.getValue()))
                .collect(Collectors.joining("\n\n"));

        return "**Lista de ingresantes registrados:**\n" + grouped;
    }

    private String formatIngresantes(List<IngresanteInfo> ingresantes) {
        return ingresantes.stream()
                .map(ingresante -> "- " + ingresante.nombre() + " - " + ingresante.universidad() + " - " + ingresante.carrera() + " (" + ingresante.tipoSeleccion() + ")")
                .collect(Collectors.joining("\n"));
    }

    private List<IngresanteInfo> parseIngresantes(String context) {
        List<IngresanteInfo> ingresantes = new ArrayList<>();

        for (String line : context.split("\\R")) {
            if (!line.startsWith("- ") || !line.contains(" | ")) {
                continue;
            }

            String[] parts = line.substring(2).split("\\|");
            if (parts.length < 5) {
                continue;
            }

            ingresantes.add(new IngresanteInfo(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    parts[4].trim()
            ));
        }

        return ingresantes;
    }

    private String extractSingleLine(String context, String label, String fallback, String format) {
        String prefix = label + ": ";
        for (String line : context.split("\\R")) {
            if (line.startsWith(prefix)) {
                String value = line.substring(prefix.length()).trim();
                if (!value.isBlank()) {
                    return format.formatted(value);
                }
            }
        }
        return fallback;
    }

    private String normalize(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private record IngresanteInfo(String nombre, String universidad, String carrera, String anio, String tipoSeleccion) {
    }
}
