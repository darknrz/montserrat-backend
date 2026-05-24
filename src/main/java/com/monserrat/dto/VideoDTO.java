package com.monserrat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    @NotBlank(message = "El tipo de medio es obligatorio")
    private String mediaType;

    @NotBlank(message = "La URL del medio es obligatoria")
    private String mediaUrl;

    @NotBlank(message = "El publicId es obligatorio")
    private String publicId;

    private String thumbnailUrl;
    private String formato;

    @NotBlank(message = "El tag es obligatorio")
    private String tag;

    @NotBlank(message = "El color del tag es obligatorio")
    private String tagColor;

    private Boolean activo;
    private Integer orden;
}
