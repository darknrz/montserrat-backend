package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "anuncios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anuncio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false, length = 80)
    private String verMasTexto;

    @Column(nullable = true, length = 500)
    private String attachmentUrl;

    @Column(nullable = true, length = 500)
    private String attachmentPublicId;

    @Column(nullable = true, length = 50)
    private String attachmentResourceType;

    @Column(nullable = true, length = 100)
    private String attachmentMimeType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean mostrarEnPopup = true;

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
