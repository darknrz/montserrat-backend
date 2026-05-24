package com.monserrat.service;

import com.monserrat.dto.RedSocialDTO;
import com.monserrat.entity.RedSocial;
import com.monserrat.repository.RedSocialRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedSocialService {

    private final RedSocialRepository redSocialRepository;

    public List<RedSocialDTO> getAll() {
        return redSocialRepository.findByActivoTrueOrderByOrdenAsc()
                .stream().map(this::toDTO).toList();
    }

    public RedSocialDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public RedSocialDTO create(RedSocialDTO dto) {
        RedSocial rs = RedSocial.builder()
                .nombre(dto.getNombre())
                .icono(dto.getIcono())
                .url(dto.getUrl())
                .orden(dto.getOrden() != null ? dto.getOrden() : 0)
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
        return toDTO(redSocialRepository.save(rs));
    }

    @Transactional
    public RedSocialDTO update(Long id, RedSocialDTO dto) {
        RedSocial rs = findOrThrow(id);
        rs.setNombre(dto.getNombre());
        rs.setIcono(dto.getIcono());
        rs.setUrl(dto.getUrl());
        if (dto.getOrden() != null) rs.setOrden(dto.getOrden());
        if (dto.getActivo() != null) rs.setActivo(dto.getActivo());
        return toDTO(redSocialRepository.save(rs));
    }

    @Transactional
    public void delete(Long id) {
        RedSocial rs = findOrThrow(id);
        rs.setActivo(false);
        redSocialRepository.save(rs);
    }

    private RedSocial findOrThrow(Long id) {
        return redSocialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Red social no encontrada: " + id));
    }

    private RedSocialDTO toDTO(RedSocial rs) {
        return RedSocialDTO.builder()
                .id(rs.getId())
                .nombre(rs.getNombre())
                .icono(rs.getIcono())
                .url(rs.getUrl())
                .activo(rs.getActivo())
                .orden(rs.getOrden())
                .build();
    }
}