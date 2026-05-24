package com.monserrat.service;

import com.monserrat.dto.chatbot.ChatbotCreateConversationResponse;
import com.monserrat.dto.chatbot.ChatbotMessageDTO;
import com.monserrat.dto.chatbot.ChatbotSocketResponse;
import com.monserrat.entity.ChatbotConversation;
import com.monserrat.entity.ChatbotLead;
import com.monserrat.entity.ChatbotMessage;
import com.monserrat.repository.ChatbotConversationRepository;
import com.monserrat.repository.ChatbotLeadRepository;
import com.monserrat.repository.ChatbotMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(9\\d{8}|\\+?51\\s?9\\d{8})");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    private final ChatbotConversationRepository conversationRepository;
    private final ChatbotMessageRepository messageRepository;
    private final ChatbotLeadRepository leadRepository;
    private final ChatbotKnowledgeService knowledgeService;
    private final GroqAiService groqAiService;
    private final ChatbotGuardService chatbotGuardService;

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

        ChatbotMessageAnalysis analysis = resolveAnalysisWithConversationContext(conversation.getId(), chatbotGuardService.analyze(text));
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

        return groqAiService.answer(text, knowledgeService.buildContext(), analysis.intent(), conversation.getNombreVisitante());
    }

    private ChatbotMessageAnalysis resolveAnalysisWithConversationContext(Long conversationId, ChatbotMessageAnalysis analysis) {
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

}
