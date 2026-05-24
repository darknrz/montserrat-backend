package com.monserrat.dto.chatbot;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatbotSocketResponse {
    private String type;
    private Long conversationId;
    private String sender;
    private String text;
    private String intent;
    private Long messageId;
}
