package com.monserrat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadResponse {
    private String publicId;
    private String resourceType;
    private String secureUrl;
    private String thumbnailUrl;
    private String format;
    private Integer width;
    private Integer height;
    private Long bytes;
    private Integer duration;
    private String originalFilename;
}
