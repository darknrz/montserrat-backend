package com.monserrat.repository;

import com.monserrat.entity.ChatbotConversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, Long> {
}
