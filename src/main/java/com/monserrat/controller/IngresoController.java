package com.monserrat.controller;

import com.monserrat.dto.IngresoDTO;
import com.monserrat.service.IngresoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingresantes")
@RequiredArgsConstructor
public class IngresoController {

    private final IngresoService ingresoService;

    /**
     * GET /api/ingresantes
     * GET /api/ingresantes?anio=2025
     * GET /api/ingresantes?universidad=UNCP
     * Público — el frontend filtra desde aquí
     */
    @GetMapping
    public ResponseEntity<List<IngresoDTO>> getAll(
            @RequestParam(required = false) String anio,
            @RequestParam(required = false) String universidad) {

        if (anio != null && !anio.isBlank()) {
            return ResponseEntity.ok(ingresoService.getByAnio(anio));
        }
        if (universidad != null && !universidad.isBlank()) {
            return ResponseEntity.ok(ingresoService.getByUniversidad(universidad));
        }
        return ResponseEntity.ok(ingresoService.getAll());
    }

    /**
     * GET /api/ingresantes/{id}
     * Público
     */
    @GetMapping("/{id}")
    public ResponseEntity<IngresoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ingresoService.getById(id));
    }

    /**
     * POST /api/ingresantes
     * Solo ADMIN — registrar nuevo ingresante
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngresoDTO> create(@Valid @RequestBody IngresoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ingresoService.create(dto));
    }

    /**
     * PUT /api/ingresantes/{id}
     * Solo ADMIN — actualizar ingresante
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngresoDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody IngresoDTO dto) {
        return ResponseEntity.ok(ingresoService.update(id, dto));
    }

    /**
     * DELETE /api/ingresantes/{id}
     * Solo ADMIN — soft delete (activo=false)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ingresoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/ingresantes/{id}/hard
     * Solo ADMIN — eliminación física de la BD
     */
    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        ingresoService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}