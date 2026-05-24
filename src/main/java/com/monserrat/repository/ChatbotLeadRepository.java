package com.monserrat.repository;

import com.monserrat.entity.ChatbotLead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotLeadRepository extends JpaRepository<ChatbotLead, Long> {
}
