package com.monserrat.controller;

import com.monserrat.dto.chatbot.ChatbotCreateConversationResponse;
import com.monserrat.dto.chatbot.ChatbotMessageDTO;
import com.monserrat.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/conversations")
    public ResponseEntity<ChatbotCreateConversationResponse> createConversation() {
        return ResponseEntity.ok(chatbotService.createConversation());
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<ChatbotMessageDTO>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(chatbotService.getHistory(id));
    }
}
