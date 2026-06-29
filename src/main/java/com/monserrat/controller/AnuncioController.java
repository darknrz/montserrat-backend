package com.monserrat.controller;

import com.monserrat.dto.AnuncioDTO;
import com.monserrat.service.AnuncioService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anuncios")
@RequiredArgsConstructor
public class AnuncioController {

    private final AnuncioService anuncioService;

    @PermitAll
    @GetMapping
    public ResponseEntity<List<AnuncioDTO>> getAll() {
        return ResponseEntity.ok(anuncioService.getAllActive());
    }

    @PermitAll
    @GetMapping("/{id}")
    public ResponseEntity<AnuncioDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(anuncioService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnuncioDTO> create(@Valid @RequestBody AnuncioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anuncioService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnuncioDTO> update(@PathVariable Long id, @Valid @RequestBody AnuncioDTO dto) {
        return ResponseEntity.ok(anuncioService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        anuncioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
