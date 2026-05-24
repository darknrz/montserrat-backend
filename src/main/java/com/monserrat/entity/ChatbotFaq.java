package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chatbot_faqs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotFaq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String pregunta;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String respuesta;

    @Column(length = 80)
    private String categoria;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;
}
