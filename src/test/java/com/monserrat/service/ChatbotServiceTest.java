package com.monserrat.service;

import com.monserrat.dto.chatbot.ChatbotSocketResponse;
import com.monserrat.entity.ChatbotConversation;
import com.monserrat.entity.ChatbotMessage;
import com.monserrat.repository.ChatbotConversationRepository;
import com.monserrat.repository.ChatbotLeadRepository;
import com.monserrat.repository.ChatbotMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private ChatbotConversationRepository conversationRepository;

    @Mock
    private ChatbotMessageRepository messageRepository;

    @Mock
    private ChatbotLeadRepository leadRepository;

    @Mock
    private ChatbotKnowledgeService knowledgeService;

    @Mock
    private GroqAiService groqAiService;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = new ChatbotService(
                conversationRepository,
                messageRepository,
                leadRepository,
                knowledgeService,
                groqAiService,
                new ChatbotGuardService()
        );
    }

    @Test
    void processUserMessageDoesNotCallAiForNoise() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        AtomicLong ids = new AtomicLong(10);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(1L, "asdfgh ???");

        assertThat(responses).hasSize(2);
        assertThat(responses.get(1).getText()).contains("No te entendi bien");
        assertThat(responses.get(1).getIntent()).isEqualTo("NO_ENTENDIDO");
        verifyNoInteractions(knowledgeService, groqAiService);
        verify(leadRepository, never()).save(any());
    }

    @Test
    void processUserMessageStoresNameAndGreetsVisitor() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        AtomicLong ids = new AtomicLong(20);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(1L, "me llamo elvis");

        assertThat(conversation.getNombreVisitante()).isEqualTo("Elvis");
        assertThat(responses.get(1).getText()).contains("Hola **Elvis**");
        assertThat(responses.get(1).getIntent()).isEqualTo("PRESENTACION");
        verifyNoInteractions(knowledgeService, groqAiService);
    }

    @Test
    void processUserMessageUsesPreviousIntentForYearFollowUp() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        ChatbotMessage previousUserMessage = ChatbotMessage.builder()
                .id(1L)
                .conversacion(conversation)
                .emisor("USER")
                .mensaje("ingresantes")
                .intencion("INGRESANTES")
                .build();
        AtomicLong ids = new AtomicLong(30);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversacionIdOrderByCreadoEnAsc(1L)).thenReturn(List.of(previousUserMessage));
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });
        when(knowledgeService.buildContext()).thenReturn("""
                INGRESANTES
                - Ana Torres | UNCP | Medicina | 2025 | 1ra Seleccion
                - Maria Quispe | UNI | Derecho | 2024 | Ordinario
                """);
        when(groqAiService.answer(any(), any(), any(), any())).thenCallRealMethod();

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(1L, "en el ano 2025");

        assertThat(responses.get(1).getIntent()).isEqualTo("INGRESANTES");
        assertThat(responses.get(1).getText()).contains("Ingresantes registrados en 2025:");
        assertThat(responses.get(1).getText()).contains("Ana Torres");
        assertThat(responses.get(1).getText()).doesNotContain("Maria Quispe");
        verifyNoMoreInteractions(leadRepository);
    }
}
