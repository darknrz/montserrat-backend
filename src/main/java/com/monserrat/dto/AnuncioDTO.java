package com.monserrat.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnuncioDTO {
    private Long id;
    private String titulo;
    private String mensaje;
    private String verMasTexto;
    private String attachmentUrl;
    private String attachmentPublicId;
    private String attachmentResourceType;
    private String attachmentMimeType;
    private String imageUrl;
    private String imagePublicId;
    private String imageMimeType;
    private Boolean mostrarEnPopup;
    private Boolean activo;
    private Integer orden;
    private String expiresAt;
}
