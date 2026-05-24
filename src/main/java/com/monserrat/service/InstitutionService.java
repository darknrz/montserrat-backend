package com.monserrat.service;

import com.monserrat.dto.InstitutionDTO;
import com.monserrat.entity.Institution;
import com.monserrat.repository.InstitutionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    // El frontend siempre pedirá el registro con id=1
    public InstitutionDTO getInstitution() {
        Institution inst = institutionRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Datos institucionales no encontrados"));
        return toDTO(inst);
    }

    @Transactional
    public InstitutionDTO update(Long id, InstitutionDTO dto) {
        Institution inst = institutionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Institución no encontrada: " + id));

        inst.setNombre(dto.getNombre());
        inst.setDireccion(dto.getDireccion());
        inst.setCiudad(dto.getCiudad());
        inst.setDistrito(dto.getDistrito());
        inst.setAnioFundacion(dto.getAnioFundacion());
        inst.setTelefono(dto.getTelefono());
        inst.setEmail(dto.getEmail());
        inst.setNiveles(dto.getNiveles());
        inst.setTipo(dto.getTipo());
        inst.setMision(dto.getMision());
        inst.setVision(dto.getVision());
        inst.setDescripcion(dto.getDescripcion());
        inst.setLogoUrl(dto.getLogoUrl());
        inst.setHorarioAtencion(dto.getHorarioAtencion());

        return toDTO(institutionRepository.save(inst));
    }

    // ── Mapper ──────────────────────────────────────────────────────────────
    private InstitutionDTO toDTO(Institution inst) {
        return InstitutionDTO.builder()
                .id(inst.getId())
                .nombre(inst.getNombre())
                .direccion(inst.getDireccion())
                .ciudad(inst.getCiudad())
                .distrito(inst.getDistrito())
                .anioFundacion(inst.getAnioFundacion())
                .telefono(inst.getTelefono())
                .email(inst.getEmail())
                .niveles(inst.getNiveles())
                .tipo(inst.getTipo())
                .mision(inst.getMision())
                .vision(inst.getVision())
                .descripcion(inst.getDescripcion())
                .logoUrl(inst.getLogoUrl())
                .horarioAtencion(inst.getHorarioAtencion())
                .build();
    }
}