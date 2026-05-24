package com.monserrat.controller;

import com.monserrat.dto.VideoDTO;
import com.monserrat.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /**
     * GET /api/videos
     * GET /api/videos?tag=Logros
     * Público — el frontend carga el carrusel desde aquí
     */
    @GetMapping
    public ResponseEntity<List<VideoDTO>> getAll(
            @RequestParam(required = false) String tag) {

        if (tag != null && !tag.isBlank()) {
            return ResponseEntity.ok(videoService.getByTag(tag));
        }
        return ResponseEntity.ok(videoService.getAll());
    }

    /**
     * GET /api/videos/{id}
     * Público
     */
    @GetMapping("/{id}")
    public ResponseEntity<VideoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getById(id));
    }

    /**
     * POST /api/videos
     * Solo ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VideoDTO> create(@Valid @RequestBody VideoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(videoService.create(dto));
    }

    /**
     * PUT /api/videos/{id}
     * Solo ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VideoDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody VideoDTO dto) {
        return ResponseEntity.ok(videoService.update(id, dto));
    }

    /**
     * DELETE /api/videos/{id}
     * Solo ADMIN — soft delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        videoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/videos/{id}/hard
     * Solo ADMIN — eliminación física
     */
    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        videoService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}