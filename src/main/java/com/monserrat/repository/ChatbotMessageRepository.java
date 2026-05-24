package com.monserrat.repository;

import com.monserrat.entity.ChatbotMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {
    List<ChatbotMessage> findByConversacionIdOrderByCreadoEnAsc(Long conversationId);
}
