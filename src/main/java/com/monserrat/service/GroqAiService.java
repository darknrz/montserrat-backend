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
import java.text.Normalizer;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        String directAnswer = directAnswer(userMessage, context);
        if (directAnswer != null) {
            return directAnswer;
        }

        if (apiKey == null || apiKey.isBlank()) {
            return fallbackAnswer(userMessage, context);
        }

        try {
            Map<String, Object> payload = Map.of(
                    "model", model,
                    "temperature", 0.2,
                    "max_tokens", 450,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt(context)),
                            Map.of("role", "user", "content", userMessage)
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(25))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return fallbackAnswer(userMessage, context);
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception ignored) {
            return fallbackAnswer(userMessage, context);
        }
    }

    private String systemPrompt(String context) {
        return """
                Eres el Asistente Monserrat de la I.E.P. Nuestra Senora de Monserrat de Huancayo.
                Responde en espanol claro, amable y breve.
                Usa solamente la informacion institucional incluida en el contexto.
                No inventes costos, vacantes, requisitos no definidos ni fechas no incluidas.
                Si preguntan por pensiones, costos o informacion no disponible, deriva al correo institucional.
                Si detectas interes de matricula, pide nombre y telefono de contacto.
                Si preguntan por ubicacion, horario o correo, responde de forma directa con esos datos.
                Si preguntan por ingresantes de un ano, menciona algunos nombres del ano solicitado si estan en el contexto.
                Evita responder con una frase generica si la respuesta esta claramente en el contexto.

                CONTEXTO:
                """ + context;
    }

    private String directAnswer(String userMessage, String context) {
        String text = normalize(userMessage);

        if (text.contains("matricula") || text.contains("vacante")) {
            return "Para informacion de matricula, escribenos a monserratcomplejoeducativo@gmail.com o visitanos en Jr. Cajamarca #563, Huancayo.";
        }
        if (text.contains("pension") || text.contains("costo") || text.contains("precio")) {
            return "Para informacion sobre pensiones o costos, comunicate directamente al correo monserratcomplejoeducativo@gmail.com.";
        }
        if (text.contains("horario")) {
            return extractSingleLine(context, "Horario", "El horario de atencion institucional es Lun-Vie 7:30am-5:00pm.", "El horario de atencion institucional es %s.");
        }
        if (text.contains("direccion") || text.contains("ubicacion") || text.contains("donde queda") || text.contains("donde esta")) {
            return extractSingleLine(context, "Direccion", "Estamos en Jr. Cajamarca #563, Huancayo. Tambien puedes escribirnos a monserratcomplejoeducativo@gmail.com.", "Estamos en %s. Tambien puedes escribirnos a monserratcomplejoeducativo@gmail.com.");
        }
        if (text.contains("correo") || text.contains("email")) {
            return extractSingleLine(context, "Correo", "Puedes escribirnos a monserratcomplejoeducativo@gmail.com.", "Puedes escribirnos a %s.");
        }
        if (text.contains("ingresaron") || text.contains("ingresante") || text.contains("alumnos") || text.contains("universidad")) {
            String byYear = answerIngresantesByYear(text, context);
            if (byYear != null) {
                return byYear;
            }
            return "La institucion registra ingresantes a universidades como UNCP, UNMSM, UNI, UPLA, UNFV, UNALM, UNH y USMP. Puedes revisar la seccion Ingresantes para ver el detalle por ano.";
        }
        return null;
    }

    private String fallbackAnswer(String userMessage, String context) {
        String text = normalize(userMessage);
        if (text.contains("matricula")) {
            return "Para informacion de matricula, escribenos a monserratcomplejoeducativo@gmail.com o visitanos en Jr. Cajamarca #563, Huancayo.";
        }
        if (text.contains("horario")) {
            return extractSingleLine(context, "Horario", "El horario de atencion institucional es Lun-Vie 7:30am-5:00pm.", "El horario de atencion institucional es %s.");
        }
        if (text.contains("direccion") || text.contains("ubicacion") || text.contains("donde queda") || text.contains("donde esta")) {
            return extractSingleLine(context, "Direccion", "Estamos en Jr. Cajamarca #563, Huancayo. Tambien puedes escribirnos a monserratcomplejoeducativo@gmail.com.", "Estamos en %s. Tambien puedes escribirnos a monserratcomplejoeducativo@gmail.com.");
        }
        if (text.contains("ingresante") || text.contains("universidad") || text.contains("alumnos") || text.contains("ingresaron")) {
            String byYear = answerIngresantesByYear(text, context);
            if (byYear != null) {
                return byYear;
            }
            return "La institucion registra ingresantes a universidades como UNCP, UNMSM, UNI, UPLA, UNFV, UNALM, UNH y USMP. Puedes revisar la seccion Ingresantes para ver el detalle por ano.";
        }
        if (text.contains("pension") || text.contains("costo") || text.contains("precio")) {
            return "Para informacion sobre pensiones o costos, comunicate directamente al correo monserratcomplejoeducativo@gmail.com.";
        }
        return "Puedo ayudarte con matricula, horarios, ubicacion, niveles, ingresantes, videos y redes institucionales. Para una consulta especifica, escribenos a monserratcomplejoeducativo@gmail.com.";
    }

    private String answerIngresantesByYear(String normalizedMessage, String context) {
        Matcher matcher = YEAR_PATTERN.matcher(normalizedMessage);
        if (!matcher.find()) {
            return null;
        }

        String year = matcher.group(1);
        List<String> names = Arrays.stream(context.split("\\R"))
                .filter(line -> line.startsWith("- "))
                .filter(line -> line.contains(" | " + year + " | "))
                .map(line -> line.substring(2).split("\\|")[0].trim())
                .limit(5)
                .toList();

        if (names.isEmpty()) {
            return "No encontre ingresantes registrados para " + year + " en este momento.";
        }

        return "En " + year + " figuran estos ingresantes: " + String.join(", ", names) + ". Si deseas, tambien puedo indicarte sus universidades y carreras.";
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
}
