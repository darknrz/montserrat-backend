package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_conversaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String canal = "WEB";

    @Column(length = 150)
    private String nombreVisitante;

    @Column(length = 30)
    private String telefono;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String estado = "ABIERTA";

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();
}
