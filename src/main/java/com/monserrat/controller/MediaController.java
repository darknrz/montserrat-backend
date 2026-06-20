package com.monserrat.controller;

import com.monserrat.dto.MediaUploadResponse;
import com.monserrat.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','ALUMNO')")
    public ResponseEntity<MediaUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "general") String folder) {
        return ResponseEntity.ok(cloudinaryService.upload(file, folder));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @RequestParam String publicId,
            @RequestParam(defaultValue = "image") String resourceType) {
        cloudinaryService.delete(publicId, resourceType);
        return ResponseEntity.noContent().build();
    }
}
