package com.monserrat.service;

import com.monserrat.dto.chatbot.ChatbotCreateConversationResponse;
import com.monserrat.dto.chatbot.ChatbotMessageDTO;
import com.monserrat.dto.chatbot.ChatbotSocketResponse;
import com.monserrat.entity.ChatbotConversation;
import com.monserrat.entity.ChatbotLead;
import com.monserrat.entity.ChatbotMessage;
import com.monserrat.entity.AsignacionAcademica;
import com.monserrat.entity.CatalogoAcademico;
import com.monserrat.entity.NotaAcademica;
import com.monserrat.entity.PensionMensual;
import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.repository.AsignacionAcademicaRepository;
import com.monserrat.repository.CatalogoAcademicoRepository;
import com.monserrat.repository.ChatbotConversationRepository;
import com.monserrat.repository.ChatbotLeadRepository;
import com.monserrat.repository.ChatbotMessageRepository;
import com.monserrat.repository.AsistenciaAcademicaRepository;
import com.monserrat.repository.NotaAcademicaRepository;
import com.monserrat.repository.PensionMensualRepository;
import com.monserrat.repository.UsuarioAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.text.Normalizer;
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
public class ChatbotService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(9\\d{8}|\\+?51\\s?9\\d{8})");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern AUTH_TOKEN_PATTERN = Pattern.compile("\\b[A-Za-z0-9]{6,30}\\b");

    // Cuantos mensajes previos (user+bot) se le pasan al LLM como contexto de conversación.
    private static final int HISTORY_WINDOW = 6;

    private static final String SYSTEM_PROMPT = """
            Eres el Asistente Monserrat, el chatbot de la I.E.P. Nuestra Señora de Monserrat.

            Reglas que NUNCA rompes:
            - Solo hablas de temas del colegio: matrícula, horarios, ubicación, pensiones,
              uniforme, ingresantes, y consultas personales de alumnos ya verificados.
            - Cuando te den "DATOS VERIFICADOS", son la única fuente de verdad. Nunca inventes,
              completes ni corrijas un valor que no esté ahí. Si falta un dato, dilo con
              naturalidad ("aún no tengo registro de eso") en vez de inventarlo.
            - Tono: cálido, cercano, profesional, como hablaría un asistente humano del colegio.
              Nunca robótico ni repetitivo — varía tu forma de saludar o cerrar cada respuesta.
            - Respuestas breves (2-5 líneas salvo que el usuario pida detalle o haya una tabla
              de datos que mostrar).
            - Si el usuario se va totalmente del tema del colegio, redirígelo con amabilidad,
              sin sonar como un mensaje de error.
            """;

    private final ChatbotConversationRepository conversationRepository;
    private final ChatbotMessageRepository messageRepository;
    private final ChatbotLeadRepository leadRepository;
    private final ChatbotKnowledgeService knowledgeService;
    private final GroqAiService groqAiService;
    private final ChatbotGuardService chatbotGuardService;
    private final UsuarioAcademicoRepository usuarioAcademicoRepository;
    private final NotaAcademicaRepository notaAcademicaRepository;
    private final AsistenciaAcademicaRepository asistenciaAcademicaRepository;
    private final PensionMensualRepository pensionMensualRepository;
    private final AsignacionAcademicaRepository asignacionAcademicaRepository;
    private final CatalogoAcademicoRepository catalogoAcademicoRepository;

    public ChatbotCreateConversationResponse createConversation() {
        ChatbotConversation conversation = conversationRepository.save(ChatbotConversation.builder()
                .canal("WEB")
                .estado("ABIERTA")
                .build());

        return ChatbotCreateConversationResponse.builder()
                .conversationId(conversation.getId())
                .status(conversation.getEstado())
                .build();
    }

    public List<ChatbotMessageDTO> getHistory(Long conversationId) {
        return messageRepository.findByConversacionIdOrderByCreadoEnAsc(conversationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public List<ChatbotSocketResponse> processUserMessage(Long conversationId, String text) {
        ChatbotConversation conversation = conversationRepository.findById(conversationId)
                .orElseGet(() -> conversationRepository.save(ChatbotConversation.builder()
                        .canal("WEB")
                        .estado("ABIERTA")
                        .build()));

        ChatbotMessageAnalysis analysis = resolveAnalysisWithConversationContext(conversation.getId(), chatbotGuardService.analyze(text), text);
        if (analysis.hasVisitorName()) {
            conversation.setNombreVisitante(analysis.visitorName());
            conversationRepository.save(conversation);
        }

        ChatbotMessage userMessage = messageRepository.save(ChatbotMessage.builder()
                .conversacion(conversation)
                .emisor("USER")
                .mensaje(text)
                .intencion(analysis.intent())
                .confianza(analysis.confidence())
                .build());

        maybeCreateLead(text, userMessage.getIntencion());

        String answer = resolveAnswer(text, conversation, analysis);

        ChatbotMessage botMessage = messageRepository.save(ChatbotMessage.builder()
                .conversacion(conversation)
                .emisor("BOT")
                .mensaje(answer)
                .intencion(userMessage.getIntencion())
                .confianza(analysis.hasDirectResponse() ? analysis.confidence() : BigDecimal.valueOf(0.80))
                .build());

        return List.of(
                ChatbotSocketResponse.builder()
                        .type("message")
                        .conversationId(conversation.getId())
                        .sender("user")
                        .text(userMessage.getMensaje())
                        .intent(userMessage.getIntencion())
                        .messageId(userMessage.getId())
                        .build(),
                ChatbotSocketResponse.builder()
                        .type("message")
                        .conversationId(conversation.getId())
                        .sender("bot")
                        .text(botMessage.getMensaje())
                        .intent(botMessage.getIntencion())
                        .messageId(botMessage.getId())
                        .build()
        );
    }

    private void maybeCreateLead(String message, String intent) {
        Matcher phoneMatcher = PHONE_PATTERN.matcher(message);
        Matcher emailMatcher = EMAIL_PATTERN.matcher(message);
        boolean leadIntent = "MATRICULA".equals(intent) || "COSTOS".equals(intent) || phoneMatcher.find() || emailMatcher.find();

        if (!leadIntent) return;

        String phone = phoneMatcher.reset().find() ? phoneMatcher.group() : null;
        String email = emailMatcher.reset().find() ? emailMatcher.group() : null;

        leadRepository.save(ChatbotLead.builder()
                .telefono(phone)
                .correo(email)
                .interes(intent)
                .mensaje(message)
                .estado("NUEVO")
                .build());
    }

    private String resolveAnswer(String text, ChatbotConversation conversation, ChatbotMessageAnalysis analysis) {
        if (analysis.hasDirectResponse()) {
            return analysis.directResponse();
        }

        if ("PRESENTACION".equals(analysis.intent()) && analysis.hasVisitorName()) {
            return "Hola **" + analysis.visitorName() + "**, gusto en saludarte. Puedo ayudarte con **matricula**, **horarios**, **ubicacion**, **pensiones**, **uniforme** o **ingresantes**.";
        }

        if (isPersonalAcademicIntent(analysis.intent())) {
            return resolvePersonalAcademicAnswer(text, analysis.intent(), conversation);
        }

        return groqAiService.answer(text, knowledgeService.buildContext(), analysis.intent(), conversation.getNombreVisitante());
    }

    private ChatbotMessageAnalysis resolveAnalysisWithConversationContext(Long conversationId, ChatbotMessageAnalysis analysis, String text) {
        if (isTopicReset(text)) {
            return new ChatbotMessageAnalysis(
                    "AYUDA",
                    BigDecimal.valueOf(0.80),
                    "Claro. Cambiemos de tema. Puedo ayudarte con matricula, horarios, ubicacion, pensiones, uniforme, ingresantes o consultas personales verificadas del alumno."
            );
        }

        if (("NO_ENTENDIDO".equals(analysis.intent()) || "GENERAL".equals(analysis.intent()) || "FUERA_DE_TEMA".equals(analysis.intent()))
                && looksLikeAuthFollowUp(text)) {
            Optional<String> previousPersonalIntent = findLastUsefulIntent(conversationId)
                    .filter(this::isPersonalAcademicIntent);
            if (previousPersonalIntent.isPresent()) {
                return new ChatbotMessageAnalysis(previousPersonalIntent.get(), BigDecimal.valueOf(0.80), null);
            }
        }

        if (("NO_ENTENDIDO".equals(analysis.intent()) || "GENERAL".equals(analysis.intent()))
                && looksLikeConversationalFollowUp(text)) {
            Optional<String> previousIntent = findLastUsefulIntent(conversationId)
                    .filter(intent -> !"AYUDA".equals(intent))
                    .filter(intent -> !"SALUDO".equals(intent))
                    .filter(intent -> !"CONVERSACION".equals(intent))
                    .filter(intent -> !"PRESENTACION".equals(intent));
            if (previousIntent.isPresent()) {
                return new ChatbotMessageAnalysis(previousIntent.get(), BigDecimal.valueOf(0.68), null);
            }
        }

        if (!"SEGUIMIENTO".equals(analysis.intent())) {
            return analysis;
        }

        return findLastUsefulIntent(conversationId)
                .map(intent -> new ChatbotMessageAnalysis(intent, BigDecimal.valueOf(0.70), null))
                .orElseGet(() -> new ChatbotMessageAnalysis(
                        "NO_ENTENDIDO",
                        BigDecimal.valueOf(0.25),
                        "No te entendi bien. Si preguntas por un ano, dime el tema. Por ejemplo: ingresantes 2025."
                ));
    }

    private boolean isPersonalAcademicIntent(String intent) {
        return "PERSONAL_NOTAS".equals(intent)
                || "PERSONAL_ASISTENCIA".equals(intent)
                || "PERSONAL_PENSION".equals(intent)
                || "PERSONAL_CURSOS".equals(intent);
    }

    private boolean looksLikeAuthFollowUp(String text) {
        Matcher matcher = AUTH_TOKEN_PATTERN.matcher(text == null ? "" : text);
        int count = 0;
        while (matcher.find()) {
            count++;
            if (count >= 2) {
                return true;
            }
        }
        return false;
    }

    private boolean isTopicReset(String text) {
        String normalized = normalizeForMatch(text);
        return normalized.contains("hablemos de otro tema")
                || normalized.contains("cambiemos de tema")
                || normalized.contains("otro tema")
                || normalized.contains("nuevo tema");
    }

    private boolean looksLikeConversationalFollowUp(String text) {
        String normalized = normalizeForMatch(text);
        if (normalized.length() < 3) {
            return false;
        }
        return normalized.startsWith("y ")
                || normalized.startsWith("tambien")
                || normalized.startsWith("ademas")
                || normalized.startsWith("entonces")
                || normalized.startsWith("ahora")
                || normalized.contains("me puedes explicar")
                || normalized.contains("explicame")
                || normalized.contains("dime mas")
                || normalized.contains("cual fue")
                || normalized.contains("como voy")
                || normalized.contains("en ese")
                || normalized.contains("sobre eso");
    }

    private String resolvePersonalAcademicAnswer(String text, String intent, ChatbotConversation conversation) {
        Optional<UsuarioAcademico> verifiedAlumno = verifyAlumnoFromMessage(text);
        if (verifiedAlumno.isEmpty()) {
            return """
                    Para responder consultas personales necesito verificar al estudiante.

                    Envia en un solo mensaje:
                    - Nombre completo, DNI o codigo institucional
                    - Codigo de seguridad del chatbot

                    El codigo de seguridad se ve en el panel del alumno y se puede regenerar desde su cuenta.""";
        }

        UsuarioAcademico alumno = verifiedAlumno.get();
        String conversationHistory = buildConversationHistory(conversation.getId());
        return switch (intent) {
            case "PERSONAL_NOTAS" -> buildNotasAnswer(alumno, conversationHistory);
            case "PERSONAL_ASISTENCIA" -> buildAsistenciaAnswer(alumno);
            case "PERSONAL_PENSION" -> buildPensionAnswer(alumno);
            case "PERSONAL_CURSOS" -> buildCursosAnswer(alumno);
            default -> "Consulta verificada.";
        };
    }

    /**
     * Arma las ultimas HISTORY_WINDOW lineas de la conversacion como texto plano,
     * para darle contexto al LLM (quien pregunto que, y que le respondio el bot antes).
     */
    private String buildConversationHistory(Long conversationId) {
        List<ChatbotMessage> mensajes = messageRepository.findByConversacionIdOrderByCreadoEnAsc(conversationId);
        int desde = Math.max(0, mensajes.size() - HISTORY_WINDOW);
        return mensajes.subList(desde, mensajes.size()).stream()
                .map(m -> ("BOT".equals(m.getEmisor()) ? "bot: " : "alumno: ") + m.getMensaje())
                .collect(Collectors.joining("\n"));
    }

    private Optional<UsuarioAcademico> verifyAlumnoFromMessage(String text) {
        List<String> tokens = extractAuthTokens(text);
        for (String chatbotCode : tokens) {
            Optional<UsuarioAcademico> alumno = usuarioAcademicoRepository.findByCodigoChatbotIgnoreCase(chatbotCode)
                    .filter(usuario -> RolUsuario.ALUMNO.equals(usuario.getRol()))
                    .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()));

            if (alumno.isPresent()) {
                UsuarioAcademico usuario = alumno.get();
                if (hasStudentIdentifier(text, tokens, usuario)) {
                    return Optional.of(usuario);
                }
            }
        }
        return Optional.empty();
    }

    private boolean hasStudentIdentifier(String text, List<String> tokens, UsuarioAcademico usuario) {
        boolean hasDni = usuario.getDni() != null
                && tokens.stream().anyMatch(token -> usuario.getDni().equalsIgnoreCase(token));
        boolean hasInstitutionalCode = usuario.getCodigo() != null
                && tokens.stream().anyMatch(token -> usuario.getCodigo().equalsIgnoreCase(token));
        boolean hasFullName = containsStudentFullName(text, usuario);

        return hasDni || hasInstitutionalCode || hasFullName;
    }

    private boolean containsStudentFullName(String text, UsuarioAcademico usuario) {
        String normalizedText = normalizeForMatch(text);
        String normalizedName = normalizeForMatch(usuario.getNombre());
        if (normalizedName.isBlank()) {
            return false;
        }
        if (normalizedText.contains(normalizedName)) {
            return true;
        }

        return List.of(normalizedName.split("\\s+")).stream()
                .filter(part -> part.length() > 1)
                .allMatch(part -> normalizedText.contains(part));
    }

    private String normalizeForMatch(String text) {
        String normalized = Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private List<String> extractAuthTokens(String text) {
        Matcher matcher = AUTH_TOKEN_PATTERN.matcher(text == null ? "" : text);
        return matcher.results()
                .map(result -> result.group().trim())
                .filter(value -> value.length() >= 6)
                .distinct()
                .toList();
    }

    /**
     * Genera la respuesta de notas con el LLM, usando los datos verificados de la BD
     * como unica fuente de verdad. Si falla la llamada al modelo (timeout, error, etc.)
     * cae al formato de tabla fijo como red de seguridad.
     */
    private String buildNotasAnswer(UsuarioAcademico alumno, String conversationHistory) {
        List<NotaAcademica> notas = notaAcademicaRepository.findByAlumno_DniOrderByPeriodoDescCreatedAtDesc(alumno.getDni());

        if (notas.isEmpty()) {
            String prompt = """
                    Conversacion reciente:
                    %s

                    El alumno %s fue verificado correctamente, pero no tiene notas registradas
                    todavia. Comunicaselo de forma natural y ofrece ayudarlo con otra cosa
                    (asistencia, pension, etc).
                    """.formatted(conversationHistory, alumno.getNombre());
            try {
                return groqAiService.generate(SYSTEM_PROMPT, prompt);
            } catch (Exception e) {
                return "Verificacion correcta. Aun no hay notas registradas para **" + alumno.getNombre() + "**.";
            }
        }

        AcademicCatalogLabels labels = buildAcademicCatalogLabels();

        try {
            String datosVerificados = notas.stream()
                    .limit(12)
                    .map(n -> "- " + labelArea(n, labels) + " | " + formatPeriodo(n.getPeriodo())
                            + " | " + labelCompetencia(n.getCompetenciaId(), labels) + " | Nota: " + formatNota(n.getValor()))
                    .collect(Collectors.joining("\n"));

            String prompt = """
                    Conversacion reciente:
                    %s

                    DATOS VERIFICADOS (alumno: %s, identidad ya confirmada):
                    %s

                    Redacta la respuesta con estas notas. Si detectas una nota destacable (muy alta
                    o baja frente al resto) puedes mencionarlo con naturalidad. Cierra ofreciendo
                    seguir con asistencia, pension o explicar un periodo puntual, adaptando la
                    oferta a lo que se venia hablando en la conversacion.
                    """.formatted(conversationHistory, alumno.getNombre(), datosVerificados);

            return groqAiService.generate(SYSTEM_PROMPT, prompt);
        } catch (Exception e) {
            return buildNotasAnswerFallback(alumno, notas, labels);
        }
    }

    /**
     * Formato de tabla fijo, usado solo si falla la llamada al LLM (red de seguridad).
     * Es el mismo comportamiento que tenia buildNotasAnswer antes de este cambio.
     */
    private String buildNotasAnswerFallback(UsuarioAcademico alumno, List<NotaAcademica> notas, AcademicCatalogLabels labels) {
        StringBuilder answer = new StringBuilder("Verificacion correcta. Estas son las ultimas notas de **")
                .append(alumno.getNombre())
                .append("**:\n\n")
                .append("| Area curricular | Periodo | Competencia | Nota |\n")
                .append("|---|---|---|---|\n");

        notas.stream()
                .limit(12)
                .forEach(nota -> answer.append("| ")
                        .append(escapeTable(labelArea(nota, labels)))
                        .append(" | ")
                        .append(escapeTable(formatPeriodo(nota.getPeriodo())))
                        .append(" | ")
                        .append(escapeTable(labelCompetencia(nota.getCompetenciaId(), labels)))
                        .append(" | ")
                        .append(formatNota(nota.getValor()))
                        .append(" |\n"));

        answer.append("\nSi quieres, puedo seguir con asistencia, pension o explicarte un periodo especifico.");

        return answer.toString().trim();
    }

    private AcademicCatalogLabels buildAcademicCatalogLabels() {
        List<CatalogoAcademico> catalogos = catalogoAcademicoRepository.findAllByOrderByOrdenAscIdAsc();
        Map<String, String> competencias = catalogos.stream()
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .filter(item -> "COMPETENCIA".equals(item.getTipo()))
                .collect(Collectors.toMap(
                        item -> item.getNivel() + "|" + item.getCodigo(),
                        CatalogoAcademico::getNombre,
                        (left, right) -> left
                ));
        Map<String, String> areas = catalogos.stream()
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .filter(item -> "AREA_CURRICULAR".equals(item.getTipo()) || "CURSO".equals(item.getTipo()))
                .collect(Collectors.toMap(
                        item -> item.getNivel() + "|" + item.getCodigo(),
                        CatalogoAcademico::getNombre,
                        (left, right) -> left
                ));
        return new AcademicCatalogLabels(competencias, areas);
    }

    private String labelArea(NotaAcademica nota, AcademicCatalogLabels labels) {
        if (nota.getCurso() == null) {
            return "-";
        }
        String curso = nota.getCurso().name();
        if (nota.getAlumno() != null && nota.getAlumno().getNivelEducativo() != null) {
            String nivel = nota.getAlumno().getNivelEducativo().name();
            String label = labels.areas().get(nivel + "|" + curso);
            if (label != null && !label.isBlank()) {
                return label;
            }
        }
        return nota.getCurso().getNombre();
    }

    private String labelCompetencia(String competenciaId, AcademicCatalogLabels labels) {
        if (competenciaId == null || competenciaId.isBlank()) {
            return "-";
        }
        Optional<String> found = labels.competencias().entrySet().stream()
                .filter(entry -> entry.getKey().endsWith("|" + competenciaId))
                .map(Map.Entry::getValue)
                .findFirst();
        return found.orElse(competenciaId);
    }

    private String formatPeriodo(String periodo) {
        if (periodo == null || periodo.isBlank()) {
            return "-";
        }
        return periodo.replace("_", " ").toLowerCase(Locale.ROOT)
                .replace("bimestre", "Bimestre")
                .replace("general", "General");
    }

    private String formatNota(Double value) {
        if (value == null) {
            return "-";
        }
        int numericValue = value.intValue();
        if (value % 1 == 0 && numericValue >= 1 && numericValue <= 4) {
            return switch (numericValue) {
                case 4 -> "AD";
                case 3 -> "A";
                case 2 -> "B";
                case 1 -> "C";
                default -> "-";
            };
        }
        if (value % 1 == 0) {
            return String.valueOf(numericValue);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private String escapeTable(String value) {
        return value == null ? "" : value.replace("|", "/");
    }

    private String buildCursosAnswer(UsuarioAcademico alumno) {
        List<AsignacionAcademica> asignaciones = asignacionAcademicaRepository.findByAlumno_DniAndActivoTrue(alumno.getDni())
                .stream()
                .sorted(Comparator.comparing(asignacion -> asignacion.getCurso() == null ? "" : asignacion.getCurso().getNombre()))
                .toList();

        if (asignaciones.isEmpty()) {
            return "Verificacion correcta. Aun no hay cursos registrados para **" + alumno.getNombre() + "**.";
        }

        AcademicCatalogLabels labels = buildAcademicCatalogLabels();
        StringBuilder answer = new StringBuilder("Verificacion correcta. Estos son los cursos de **")
                .append(alumno.getNombre())
                .append("**:\n\n")
                .append("| Curso | Docente | Grado | Seccion |\n")
                .append("|---|---|---|---|\n");

        asignaciones.forEach(asignacion -> answer.append("| ")
                .append(escapeTable(labelCurso(asignacion, labels)))
                .append(" | ")
                .append(escapeTable(asignacion.getDocente() == null ? "-" : asignacion.getDocente().getNombre()))
                .append(" | ")
                .append(escapeTable(asignacion.getGrado() == null ? "-" : asignacion.getGrado().getNombre()))
                .append(" | ")
                .append(escapeTable(asignacion.getSeccion() == null ? "-" : asignacion.getSeccion().name()))
                .append(" |\n"));

        answer.append("\nSi quieres, tambien puedo ayudarte con notas, asistencia o pension.");
        return answer.toString().trim();
    }

    private String labelCurso(AsignacionAcademica asignacion, AcademicCatalogLabels labels) {
        if (asignacion.getCurso() == null) {
            return "-";
        }
        String curso = asignacion.getCurso().name();
        if (asignacion.getNivelEducativo() != null) {
            String label = labels.areas().get(asignacion.getNivelEducativo().name() + "|" + curso);
            if (label != null && !label.isBlank()) {
                return label;
            }
        }
        return asignacion.getCurso().getNombre();
    }

    private String buildAsistenciaAnswer(UsuarioAcademico alumno) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        var asistencias = asistenciaAcademicaRepository.findByAlumno_DniOrderByFechaDesc(alumno.getDni());
        if (asistencias.isEmpty()) {
            return "Verificacion correcta. Aun no hay asistencias registradas para **" + alumno.getNombre() + "**.";
        }

        long presentes = asistencias.stream().filter(a -> a.getEstado() != null && "PRESENTE".equals(a.getEstado().name())).count();
        StringBuilder answer = new StringBuilder("Verificacion correcta. Asistencia de **")
                .append(alumno.getNombre())
                .append("**: ")
                .append(presentes)
                .append("/")
                .append(asistencias.size())
                .append(" registros presentes.\nUltimos registros:\n");

        asistencias.stream()
                .limit(6)
                .forEach(asistencia -> answer.append("- ")
                        .append(asistencia.getFecha() == null ? "Sin fecha" : asistencia.getFecha().format(formatter))
                        .append(": ")
                        .append(asistencia.getEstado())
                        .append(asistencia.getObservacion() == null || asistencia.getObservacion().isBlank() ? "" : " - " + asistencia.getObservacion())
                        .append("\n"));

        return answer.toString().trim();
    }

    private String buildPensionAnswer(UsuarioAcademico alumno) {
        int year = Year.now().getValue();
        List<PensionMensual> pensiones = pensionMensualRepository.findByAlumno_DniAndAnio(alumno.getDni(), year)
                .stream()
                .sorted(Comparator.comparing(PensionMensual::getMes))
                .toList();

        String estadoActual = Boolean.TRUE.equals(alumno.getPensionPagada()) ? "pagada" : "pendiente";
        StringBuilder answer = new StringBuilder("Verificacion correcta. Pension actual de **")
                .append(alumno.getNombre())
                .append("**: **")
                .append(estadoActual)
                .append("**.");

        if (alumno.getPensionObservacion() != null && !alumno.getPensionObservacion().isBlank()) {
            answer.append("\nObservacion: ").append(alumno.getPensionObservacion());
        }

        if (!pensiones.isEmpty()) {
            answer.append("\nDetalle ").append(year).append(":\n");
            pensiones.forEach(pension -> answer.append("- Mes ")
                    .append(pension.getMes())
                    .append(": ")
                    .append(Boolean.TRUE.equals(pension.getPagada()) ? "pagada" : "pendiente")
                    .append(pension.getObservacion() == null || pension.getObservacion().isBlank() ? "" : " - " + pension.getObservacion())
                    .append("\n"));
        }

        return answer.toString().trim();
    }

    private Optional<String> findLastUsefulIntent(Long conversationId) {
        return messageRepository.findByConversacionIdOrderByCreadoEnAsc(conversationId)
                .stream()
                .filter(message -> "USER".equals(message.getEmisor()))
                .map(ChatbotMessage::getIntencion)
                .filter(intent -> intent != null && !intent.isBlank())
                .filter(intent -> !"NO_ENTENDIDO".equals(intent))
                .filter(intent -> !"FUERA_DE_TEMA".equals(intent))
                .filter(intent -> !"SEGUIMIENTO".equals(intent))
                .reduce((previous, current) -> current);
    }

    private ChatbotMessageDTO toDto(ChatbotMessage message) {
        return ChatbotMessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversacion().getId())
                .sender(message.getEmisor().equals("BOT") ? "bot" : "user")
                .text(message.getMensaje())
                .intent(message.getIntencion())
                .createdAt(message.getCreadoEn())
                .build();
    }

    private record AcademicCatalogLabels(Map<String, String> competencias, Map<String, String> areas) {
    }

}
