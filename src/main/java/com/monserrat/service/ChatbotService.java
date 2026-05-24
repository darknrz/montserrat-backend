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
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
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

        ChatbotMessage userMessage = messageRepository.save(ChatbotMessage.builder()
                .conversacion(conversation)
                .emisor("USER")
                .mensaje(text)
                .intencion(detectIntent(text))
                .confianza(BigDecimal.valueOf(0.75))
                .build());

        maybeCreateLead(text, userMessage.getIntencion());

        String answer = groqAiService.answer(text, knowledgeService.buildContext());

        ChatbotMessage botMessage = messageRepository.save(ChatbotMessage.builder()
                .conversacion(conversation)
                .emisor("BOT")
                .mensaje(answer)
                .intencion(userMessage.getIntencion())
                .confianza(BigDecimal.valueOf(0.80))
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

    private String detectIntent(String message) {
        String text = normalize(message);
        if (text.contains("matricula") || text.contains("vacante")) return "MATRICULA";
        if (text.contains("pension") || text.contains("costo") || text.contains("precio")) return "COSTOS";
        if (text.contains("horario")) return "HORARIO";
        if (text.contains("direccion") || text.contains("ubicacion") || text.contains("donde")) return "UBICACION";
        if (text.contains("ingresante") || text.contains("universidad") || text.contains("alumnos") || text.contains("ingresaron")) return "INGRESANTES";
        if (text.contains("uniforme")) return "UNIFORME";
        return "GENERAL";
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

    private String normalize(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT);
    }
}
