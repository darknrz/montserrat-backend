package com.monserrat.controller;

import com.monserrat.dto.RedSocialDTO;
import com.monserrat.service.RedSocialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/redes-sociales")
@RequiredArgsConstructor
public class RedSocialController {

    private final RedSocialService redSocialService;

    /**
     * GET /api/redes-sociales
     * Público — el frontend carga íconos de redes desde aquí
     */
    @GetMapping
    public ResponseEntity<List<RedSocialDTO>> getAll() {
        return ResponseEntity.ok(redSocialService.getAll());
    }

    /**
     * GET /api/redes-sociales/{id}
     * Público
     */
    @GetMapping("/{id}")
    public ResponseEntity<RedSocialDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(redSocialService.getById(id));
    }

    /**
     * POST /api/redes-sociales
     * Solo ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RedSocialDTO> create(@Valid @RequestBody RedSocialDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(redSocialService.create(dto));
    }

    /**
     * PUT /api/redes-sociales/{id}
     * Solo ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RedSocialDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RedSocialDTO dto) {
        return ResponseEntity.ok(redSocialService.update(id, dto));
    }

    /**
     * DELETE /api/redes-sociales/{id}
     * Solo ADMIN — soft delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        redSocialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}