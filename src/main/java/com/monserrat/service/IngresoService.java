package com.monserrat.service;

import com.monserrat.dto.IngresoDTO;
import com.monserrat.entity.Ingreso;
import com.monserrat.repository.IngresoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngresoService {

    private final IngresoRepository ingresoRepository;

    // ── GET públicos ─────────────────────────────────────────────────────────

    public List<IngresoDTO> getAll() {
        return ingresoRepository.findByActivoTrue()
                .stream().map(this::toDTO).toList();
    }

    public List<IngresoDTO> getByAnio(String anio) {
        return ingresoRepository.findByAnioAndActivoTrue(anio)
                .stream().map(this::toDTO).toList();
    }

    public List<IngresoDTO> getByUniversidad(String siglas) {
        return ingresoRepository.findByUniversidadSiglasAndActivoTrue(siglas)
                .stream().map(this::toDTO).toList();
    }

    public IngresoDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    // ── CRUD admin ───────────────────────────────────────────────────────────

    @Transactional
    public IngresoDTO create(IngresoDTO dto) {
        Ingreso ingreso = Ingreso.builder()
                .nombre(dto.getNombre())
                .universidad(dto.getUniversidad())
                .universidadSiglas(dto.getUniversidadSiglas().toUpperCase())
                .carrera(dto.getCarrera())
                .anio(dto.getAnio())
                .tipoSeleccion(dto.getTipoSeleccion())
                .fotoUrl(dto.getFotoUrl())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
        return toDTO(ingresoRepository.save(ingreso));
    }

    @Transactional
    public IngresoDTO update(Long id, IngresoDTO dto) {
        Ingreso ingreso = findOrThrow(id);
        ingreso.setNombre(dto.getNombre());
        ingreso.setUniversidad(dto.getUniversidad());
        ingreso.setUniversidadSiglas(dto.getUniversidadSiglas().toUpperCase());
        ingreso.setCarrera(dto.getCarrera());
        ingreso.setAnio(dto.getAnio());
        ingreso.setTipoSeleccion(dto.getTipoSeleccion());
        ingreso.setFotoUrl(dto.getFotoUrl());
        if (dto.getActivo() != null) ingreso.setActivo(dto.getActivo());
        return toDTO(ingresoRepository.save(ingreso));
    }

    @Transactional
    public void delete(Long id) {
        Ingreso ingreso = findOrThrow(id);
        // Soft delete
        ingreso.setActivo(false);
        ingresoRepository.save(ingreso);
    }

    @Transactional
    public void hardDelete(Long id) {
        if (!ingresoRepository.existsById(id))
            throw new EntityNotFoundException("Ingresante no encontrado: " + id);
        ingresoRepository.deleteById(id);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Ingreso findOrThrow(Long id) {
        return ingresoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ingresante no encontrado: " + id));
    }

    private IngresoDTO toDTO(Ingreso i) {
        return IngresoDTO.builder()
                .id(i.getId())
                .nombre(i.getNombre())
                .universidad(i.getUniversidad())
                .universidadSiglas(i.getUniversidadSiglas())
                .carrera(i.getCarrera())
                .anio(i.getAnio())
                .tipoSeleccion(i.getTipoSeleccion())
                .fotoUrl(i.getFotoUrl())
                .activo(i.getActivo())
                .build();
    }
}