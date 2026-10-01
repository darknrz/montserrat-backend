package com.monserrat.service;

import com.monserrat.dto.RedSocialDTO;
import com.monserrat.entity.RedSocial;
import com.monserrat.repository.RedSocialRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedSocialService {

    private final RedSocialRepository redSocialRepository;

    public List<RedSocialDTO> getAll() {
        return redSocialRepository.findByActivoTrueOrderByOrdenAsc()
                .stream().map(this::toDTO).toList();
    }

    public List<RedSocialDTO> getAllAdmin() {
        return redSocialRepository.findAllByOrderByOrdenAscIdAsc().stream().map(this::toDTO).toList();
    }

    @Transactional
    public void reorder(List<Long> ids) {
        Map<Long, RedSocial> byId = new HashMap<>();
        redSocialRepository.findAllById(ids).forEach(r -> byId.put(r.getId(), r));
        int i = 0;
        for (Long id : ids) {
            RedSocial r = byId.get(id);
            if (r != null) {
                r.setOrden(i++);
                redSocialRepository.save(r);
            }
        }
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
                .orden(redSocialRepository.findAllByOrderByOrdenAscIdAsc().stream().mapToInt(r -> r.getOrden() != null ? r.getOrden() : 0).max().orElse(-1) + 1)
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
        if (dto.getActivo() != null) rs.setActivo(dto.getActivo());
        return toDTO(redSocialRepository.save(rs));
    }

    @Transactional
    public void delete(Long id) {
        redSocialRepository.delete(findOrThrow(id));
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