package com.monserrat.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.monserrat.dto.MediaUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 100L * 1024 * 1024;

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:monserrat}")
    private String folder;

    public MediaUploadResponse upload(MultipartFile file, String targetFolder) {
        validateConfiguration();
        validateFile(file);

        String resourceType = detectResourceType(file);
        validateFileSize(file, resourceType);

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", buildFolder(targetFolder),
                            "resource_type", resourceType,
                            "public_id", UUID.randomUUID().toString(),
                            "overwrite", false,
                            "use_filename", true,
                            "unique_filename", true
                    )
            );

            String uploadedResourceType = stringValue(uploadResult.get("resource_type"));
            String secureUrl = stringValue(uploadResult.get("secure_url"));
            String publicId = stringValue(uploadResult.get("public_id"));

            return MediaUploadResponse.builder()
                    .publicId(publicId)
                    .resourceType(uploadedResourceType)
                    .secureUrl(secureUrl)
                    .thumbnailUrl(buildThumbnailUrl(uploadedResourceType, publicId, secureUrl))
                    .format(stringValue(uploadResult.get("format")))
                    .width(intValue(uploadResult.get("width")))
                    .height(intValue(uploadResult.get("height")))
                    .bytes(longValue(uploadResult.get("bytes")))
                    .duration(intValue(uploadResult.get("duration")))
                    .originalFilename(file.getOriginalFilename())
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo subir el archivo a Cloudinary", e);
        }
    }

    public void delete(String publicId, String resourceType) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        validateConfiguration();

        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", normalizeResourceType(resourceType))
            );
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo eliminar el archivo de Cloudinary", e);
        }
    }

    private void validateConfiguration() {
        Object cloudName = cloudinary.config.cloudName;
        Object apiKey = cloudinary.config.apiKey;
        Object apiSecret = cloudinary.config.apiSecret;

        if (cloudName == null || apiKey == null || apiSecret == null
                || cloudName.toString().isBlank()
                || apiKey.toString().isBlank()
                || apiSecret.toString().isBlank()) {
            throw new IllegalStateException("Cloudinary no esta configurado. Define CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY y CLOUDINARY_API_SECRET.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes seleccionar un archivo.");
        }
    }

    private void validateFileSize(MultipartFile file, String resourceType) {
        long maxBytes = "video".equals(resourceType) ? MAX_VIDEO_BYTES : MAX_IMAGE_BYTES;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("El archivo excede el tamano permitido para " + resourceType + ".");
        }
    }

    private String detectResourceType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("video/")) {
            return "video";
        }
        if (contentType != null && contentType.startsWith("image/")) {
            return "image";
        }
        throw new IllegalArgumentException("Solo se permiten imagenes y videos.");
    }

    private String buildFolder(String targetFolder) {
        if (targetFolder == null || targetFolder.isBlank()) {
            return folder;
        }
        return folder + "/" + targetFolder.trim().replace("\\", "/");
    }

    private String buildThumbnailUrl(String resourceType, String publicId, String secureUrl) {
        if ("video".equals(resourceType) && publicId != null && !publicId.isBlank()) {
            return cloudinary.url()
                    .resourceType("video")
                    .transformation(new com.cloudinary.Transformation<>()
                            .width(1200)
                            .height(675)
                            .crop("fill")
                            .quality("auto")
                            .fetchFormat("jpg")
                            .chain()
                            .page("1"))
                    .secure(true)
                    .generate(publicId + ".jpg");
        }
        return secureUrl;
    }

    private String normalizeResourceType(String resourceType) {
        return "video".equalsIgnoreCase(resourceType) ? "video" : "image";
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
