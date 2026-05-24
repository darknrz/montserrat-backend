package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_mensajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversacion_id", nullable = false)
    private ChatbotConversation conversacion;

    @Column(nullable = false, length = 20)
    private String emisor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(length = 80)
    private String intencion;

    @Column(precision = 5, scale = 2)
    private BigDecimal confianza;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();
}
