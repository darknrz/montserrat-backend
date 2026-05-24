package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String nombre;

    @Column(length = 30)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Column(length = 150)
    private String interes;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String estado = "NUEVO";

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();
}
