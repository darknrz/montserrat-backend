package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = true, length = 20)
    private String mediaType;

    @Column(nullable = true, length = 500)
    private String mediaUrl;

    @Column(nullable = true, length = 255, unique = true)
    private String publicId;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(length = 30)
    private String formato;

    @Column(nullable = false, length = 50)
    private String tag;

    @Column(nullable = false, length = 30)
    private String tagColor;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();
}