package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "redes_sociales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // facebook, tiktok, youtube, instagram
    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String icono;

    @Column(nullable = false, length = 300)
    private String url;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;
}