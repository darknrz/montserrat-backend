package com.monserrat.controller;

import com.monserrat.dto.InstitutionDTO;
import com.monserrat.service.InstitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institution")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    /**
     * GET /api/institution
     * Público — el frontend carga los datos de la IE al iniciar
     */
    @GetMapping
    public ResponseEntity<InstitutionDTO> get() {
        return ResponseEntity.ok(institutionService.getInstitution());
    }

    /**
     * PUT /api/institution/{id}
     * Solo ADMIN — actualiza los datos institucionales
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstitutionDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody InstitutionDTO dto) {
        return ResponseEntity.ok(institutionService.update(id, dto));
    }
}