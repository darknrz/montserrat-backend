package com.monserrat.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotGuardService {

    private static final String NOT_UNDERSTOOD_RESPONSE = "No te entendi bien. Puedo ayudarte con matricula, horarios, ubicacion, pensiones, uniforme o ingresantes.";
    private static final String OUT_OF_SCOPE_RESPONSE = "Solo puedo ayudarte con informacion de la I.E.P. Nuestra Senora de Monserrat. Puedes preguntarme sobre matricula, horarios, ubicacion, pensiones, uniforme o ingresantes.";

    private static final Pattern LETTER_PATTERN = Pattern.compile(".*[a-zA-Z].*");
    private static final Pattern YEAR_ONLY_PATTERN = Pattern.compile("^(?:en\\s+)?(?:el\\s+)?(?:ano\\s+)?20\\d{2}$");
    // Antes era ".*(.)\\1{4,}.*" y bloqueaba cualquier texto con 5+ caracteres repetidos,
    // incluyendo digitos. Eso hacia que un DNI valido como "30000037" (contiene "00000")
    // se marcara como ruido/spam. Ahora solo aplica a LETRAS repetidas (ej: "holaaaaa",
    // "jajajaja"), nunca a numeros, que son datos legitimos (DNI, codigos, telefonos).
    private static final Pattern REPEATED_CHAR_PATTERN = Pattern.compile(".*([a-z])\\1{4,}.*");
    private static final Pattern MANY_CONSONANTS_PATTERN = Pattern.compile(".*[bcdfghjklmnpqrstvwxyz]{5,}.*");
    private static final Pattern NAME_PATTERN = Pattern.compile("^(?:me llamo|mi nombre es|soy)\\s+([a-zA-Z ]{2,40})$");

    private static final List<IntentKeywordGroup> INTENT_KEYWORDS = List.of(
            new IntentKeywordGroup("MATRICULA", Set.of("matricula", "matricular", "inscripcion", "inscribir", "vacante", "vacantes", "admision", "postular")),
            new IntentKeywordGroup("COSTOS", Set.of("pension", "pensiones", "costo", "costos", "precio", "precios", "cuota", "mensualidad", "pago")),
            new IntentKeywordGroup("HORARIO", Set.of("horario", "atencion", "atienden", "hora", "abre", "abren", "cierra", "cierran")),
            new IntentKeywordGroup("UBICACION", Set.of("direccion", "ubicacion", "ubicar", "donde", "queda", "mapa", "local", "sede")),
            new IntentKeywordGroup("INGRESANTES", Set.of("ingresante", "ingresantes", "universidad", "universidades", "alumnos", "egresados", "ingresaron", "carrera")),
            new IntentKeywordGroup("UNIFORME", Set.of("uniforme", "uniformes", "buzo", "ropa", "vestimenta")),
            new IntentKeywordGroup("CONTACTO", Set.of("correo", "email", "telefono", "celular", "whatsapp", "contacto", "llamar")),
            new IntentKeywordGroup("INSTITUCION", Set.of("colegio", "institucion", "monserrat", "niveles", "primaria", "secundaria", "mision", "vision")),
            new IntentKeywordGroup("AYUDA", Set.of("puedes", "pudes", "puede", "hacer", "ayuda", "ayudar", "sirves", "funciones", "opciones"))
    );

    private static final Set<String> GREETINGS = Set.of("hola", "buenos", "buenas", "saludos", "hello", "hi");
    private static final Set<String> SOCIAL_WORDS = Set.of("estas", "esta", "tal", "vas", "va");
    private static final Set<String> PERSONAL_MARKERS = Set.of("mi", "mis", "mio", "mia", "mios", "mias", "yo");
    private static final Set<String> ACADEMIC_REQUEST_WORDS = Set.of("consulta", "consultar", "ver", "revisar", "saber", "mostrar", "muestra");
    private static final Set<String> PERSONAL_NOTES = Set.of("nota", "notas", "calificacion", "calificaciones", "promedio", "boleta");
    private static final Set<String> PERSONAL_ATTENDANCE = Set.of("asistencia", "asistencias", "inasistencia", "inasistencias", "faltas", "tardanzas");
    private static final Set<String> PERSONAL_PAYMENTS = Set.of("pension", "pensiones", "mensualidad", "mensualidades", "pago", "pagos", "deuda", "deudas");
    private static final Set<String> PERSONAL_COURSES = Set.of("curso", "cursos", "area", "areas", "materia", "materias", "asignatura", "asignaturas");
    private static final Set<String> FILLER_WORDS = Set.of("por", "favor", "quiero", "quisiera", "saber", "informacion", "info", "me", "puedes", "decir", "sobre", "del", "de", "la", "el", "los", "las", "un", "una", "que", "cual", "como", "cuando", "cuanto", "cuantos", "hay");
    private static final Set<String> OUT_OF_SCOPE_KEYWORDS = Set.of("futbol", "partido", "receta", "cocina", "presidente", "politica", "clima", "programacion", "codigo", "java", "python", "bitcoin", "dolar", "pelicula", "musica", "tarea");

    public ChatbotMessageAnalysis analyze(String message) {
        String normalized = normalize(message);
        List<String> words = words(normalized);

        if (isYearOnlyFollowUp(normalized)) {
            return new ChatbotMessageAnalysis("SEGUIMIENTO", BigDecimal.valueOf(0.55), null);
        }

        if (isNoise(normalized, words)) {
            return new ChatbotMessageAnalysis("NO_ENTENDIDO", BigDecimal.valueOf(0.15), NOT_UNDERSTOOD_RESPONSE);
        }

        String visitorName = extractVisitorName(normalized);
        if (visitorName != null) {
            return new ChatbotMessageAnalysis("PRESENTACION", BigDecimal.valueOf(0.85), null, visitorName);
        }

        IntentScore bestIntent = detectIntent(words);

        if (isGreetingOnly(words)) {
            return new ChatbotMessageAnalysis("SALUDO", BigDecimal.valueOf(0.70),
                    "Hola, soy el **Asistente Monserrat**. Puedo ayudarte con **matricula**, **horarios**, **ubicacion**, **pensiones**, **uniforme** o **ingresantes**.");
        }

        if (isSocialQuestion(words)) {
            return new ChatbotMessageAnalysis("CONVERSACION", BigDecimal.valueOf(0.70),
                    "Estoy bien, gracias. Soy el **Asistente Monserrat** y puedo ayudarte con informacion del colegio:\n- Matricula\n- Horarios\n- Ubicacion\n- Pensiones\n- Uniforme\n- Ingresantes");
        }

        String personalIntent = detectPersonalAcademicIntent(words);
        if (personalIntent != null) {
            return new ChatbotMessageAnalysis(personalIntent, BigDecimal.valueOf(0.90), null);
        }

        if ("AYUDA".equals(bestIntent.intent()) && bestIntent.score() > 0) {
            return new ChatbotMessageAnalysis("AYUDA", BigDecimal.valueOf(0.80), helpResponse());
        }

        if (isOutOfScope(words, bestIntent)) {
            return new ChatbotMessageAnalysis("FUERA_DE_TEMA", BigDecimal.valueOf(0.20), OUT_OF_SCOPE_RESPONSE);
        }

        if (bestIntent.score() == 0 && normalized.length() < 18) {
            return new ChatbotMessageAnalysis("NO_ENTENDIDO", BigDecimal.valueOf(0.25), NOT_UNDERSTOOD_RESPONSE);
        }

        String intent = bestIntent.score() > 0 ? bestIntent.intent() : "GENERAL";
        BigDecimal confidence = confidence(bestIntent.score(), words.size());
        return new ChatbotMessageAnalysis(intent, confidence, null);
    }

    private boolean isNoise(String normalized, List<String> words) {
        if (normalized.isBlank()) {
            return true;
        }
        if (normalized.length() < 2) {
            return true;
        }
        if (!LETTER_PATTERN.matcher(normalized).matches()) {
            return true;
        }
        if (REPEATED_CHAR_PATTERN.matcher(normalized).matches()) {
            return true;
        }
        if (words.isEmpty()) {
            return true;
        }
        long meaningfulWords = words.stream()
                .filter(word -> word.length() >= 3)
                .filter(word -> !FILLER_WORDS.contains(word))
                .count();
        if (meaningfulWords == 0 && words.size() <= 3) {
            return true;
        }

        long suspiciousWords = words.stream()
                .filter(word -> word.length() >= 5)
                .filter(this::looksLikeKeyboardNoise)
                .count();

        return meaningfulWords > 0 && suspiciousWords == meaningfulWords;
    }

    private boolean isGreetingOnly(List<String> words) {
        return !words.isEmpty() && words.stream().allMatch(word -> GREETINGS.contains(word) || FILLER_WORDS.contains(word));
    }

    private boolean isSocialQuestion(List<String> words) {
        return words.contains("como") && words.stream().anyMatch(SOCIAL_WORDS::contains);
    }

    private String detectPersonalAcademicIntent(List<String> words) {
        boolean personal = words.stream().anyMatch(PERSONAL_MARKERS::contains)
                || words.stream().anyMatch(ACADEMIC_REQUEST_WORDS::contains);
        if (!personal) {
            return null;
        }
        if (words.stream().anyMatch(PERSONAL_NOTES::contains)) {
            return "PERSONAL_NOTAS";
        }
        if (words.stream().anyMatch(PERSONAL_ATTENDANCE::contains)) {
            return "PERSONAL_ASISTENCIA";
        }
        if (words.stream().anyMatch(PERSONAL_PAYMENTS::contains)) {
            return "PERSONAL_PENSION";
        }
        if (words.stream().anyMatch(PERSONAL_COURSES::contains)) {
            return "PERSONAL_CURSOS";
        }
        return null;
    }

    private boolean isYearOnlyFollowUp(String normalized) {
        return YEAR_ONLY_PATTERN.matcher(normalized).matches();
    }

    private String helpResponse() {
        return """
                **Puedo ayudarte con consultas de la I.E.P. Nuestra Senora de Monserrat:**
                - **Matricula y vacantes**
                - **Pensiones o costos**
                - **Horario de atencion**
                - **Ubicacion y contacto**
                - **Lista de ingresantes por ano**
                - **Uniforme**

                *Ejemplos:* "ingresantes 2025", "ubicacion" o "horario de atencion".""";
    }

    private boolean isOutOfScope(List<String> words, IntentScore bestIntent) {
        boolean hasExternalTopic = words.stream().anyMatch(OUT_OF_SCOPE_KEYWORDS::contains);
        return hasExternalTopic && bestIntent.score() == 0;
    }

    private boolean looksLikeKeyboardNoise(String word) {
        return word.contains("asdf")
                || word.contains("qwer")
                || word.contains("zxcv")
                || MANY_CONSONANTS_PATTERN.matcher(word).matches();
    }

    private String extractVisitorName(String normalized) {
        Matcher matcher = NAME_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return null;
        }

        String rawName = matcher.group(1).trim();
        if (rawName.split("\\s+").length > 3) {
            return null;
        }

        String name = Arrays.stream(rawName.split("\\s+"))
                .filter(word -> !FILLER_WORDS.contains(word))
                .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse("");

        return name.isBlank() ? null : name;
    }

    private IntentScore detectIntent(List<String> words) {
        IntentScore best = new IntentScore("GENERAL", 0);

        for (IntentKeywordGroup group : INTENT_KEYWORDS) {
            int score = 0;
            for (String word : words) {
                if (FILLER_WORDS.contains(word)) {
                    continue;
                }
                if (group.keywords().contains(word)) {
                    score += 3;
                    continue;
                }
                if (word.length() >= 5 && isCloseToAnyKeyword(word, group.keywords())) {
                    score += 2;
                }
            }

            if (score > best.score()) {
                best = new IntentScore(group.intent(), score);
            }
        }

        return best;
    }

    private boolean isCloseToAnyKeyword(String word, Set<String> keywords) {
        return keywords.stream()
                .filter(keyword -> Math.abs(keyword.length() - word.length()) <= 2)
                .anyMatch(keyword -> levenshteinDistance(word, keyword) <= 2);
    }

    private BigDecimal confidence(int score, int wordCount) {
        double base = Math.min(0.95, 0.45 + (score * 0.12));
        if (wordCount <= 2 && score <= 2) {
            base = Math.min(base, 0.55);
        }
        return BigDecimal.valueOf(base).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private int levenshteinDistance(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];

        for (int i = 0; i <= right.length(); i++) {
            previous[i] = i;
        }

        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + cost
                );
            }
            int[] temp = previous;
            previous = current;
            current = temp;
        }

        return previous[right.length()];
    }

    private List<String> words(String normalized) {
        return Arrays.stream(normalized.split("[^a-z0-9]+"))
                .filter(word -> !word.isBlank())
                .toList();
    }

    private String normalize(String text) {
        String normalized = Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private record IntentKeywordGroup(String intent, Set<String> keywords) {
    }

    private record IntentScore(String intent, int score) {
    }
}
