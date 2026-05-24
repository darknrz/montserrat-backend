package com.monserrat.repository;

import com.monserrat.entity.ChatbotFaq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatbotFaqRepository extends JpaRepository<ChatbotFaq, Long> {
    List<ChatbotFaq> findByActivoTrueOrderByOrdenAsc();
}
