package com.monserrat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monserrat.dto.chatbot.ChatbotSocketRequest;
import com.monserrat.dto.chatbot.ChatbotSocketResponse;
import com.monserrat.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatbotService chatbotService;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatbotSocketRequest request = objectMapper.readValue(message.getPayload(), ChatbotSocketRequest.class);

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return;
        }

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                ChatbotSocketResponse.builder()
                        .type("typing")
                        .conversationId(request.getConversationId())
                        .sender("bot")
                        .text("typing")
                        .build()
        )));

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(
                request.getConversationId(),
                request.getMessage().trim()
        );

        for (ChatbotSocketResponse response : responses) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }
    }
}
