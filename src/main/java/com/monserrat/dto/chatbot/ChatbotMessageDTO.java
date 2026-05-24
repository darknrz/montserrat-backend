package com.monserrat.dto.chatbot;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatbotMessageDTO {
    private Long id;
    private Long conversationId;
    private String sender;
    private String text;
    private String intent;
    private LocalDateTime createdAt;
}
