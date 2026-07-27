package com.monserrat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResendEmailService {

    private final RestClient.Builder restClientBuilder;

    @Value("${resend.api.key:}")
    private String apiKey;

    @Value("${resend.from:Monserrat <onboarding@resend.dev>}")
    private String from;

    public void sendPasswordResetEmail(String to, String resetUrl) {
        String resolvedApiKey = resolveConfigValue(apiKey, "RESEND_API_KEY");
        String resolvedFrom = resolveConfigValue(from, "RESEND_FROM");

        if (resolvedApiKey == null || resolvedApiKey.isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY no esta configurada");
        }

        String html = """
                <p>Hola,</p>
                <p>Recibimos una solicitud para restablecer tu contrasena en el portal de Monserrat.</p>
                <p><a href="%s">Restablecer contrasena</a></p>
                <p>Este enlace vence en 30 minutos. Si no solicitaste este cambio, puedes ignorar este mensaje.</p>
                """.formatted(resetUrl);

        restClientBuilder.build()
                .post()
                .uri("https://api.resend.com/emails")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + resolvedApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", resolvedFrom,
                        "to", List.of(to),
                        "subject", "Restablece tu contrasena",
                        "html", html
                ))
                .retrieve()
                .toBodilessEntity();
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
}
