package com.monserrat.dto.chatbot;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatbotCreateConversationResponse {
    private Long conversationId;
    private String status;
}
