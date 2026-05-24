package com.monserrat.dto.chatbot;

import lombok.Data;

@Data
public class ChatbotSocketRequest {
    private Long conversationId;
    private String message;
}
