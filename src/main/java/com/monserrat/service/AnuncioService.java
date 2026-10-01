package com.monserrat.service;

import com.monserrat.dto.AnuncioDTO;
import com.monserrat.entity.Anuncio;
import com.monserrat.repository.AnuncioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnuncioService {

    private final AnuncioRepository anuncioRepository;

    public List<AnuncioDTO> getAllActive() {
        return anuncioRepository.findActiveValidOrderByOrdenAsc().stream().map(this::toDTO).toList();
    }

    public List<AnuncioDTO> getAllAdmin() {
        return anuncioRepository.findAllByOrderByOrdenAscIdAsc().stream().map(this::toDTO).toList();
    }

    @Transactional
    public void reorder(List<Long> ids) {
        Map<Long, Anuncio> byId = new HashMap<>();
        anuncioRepository.findAllById(ids).forEach(a -> byId.put(a.getId(), a));
        int i = 0;
        for (Long id : ids) {
            Anuncio a = byId.get(id);
            if (a != null) {
                a.setOrden(i++);
                anuncioRepository.save(a);
            }
        }
    }

    public AnuncioDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public AnuncioDTO create(AnuncioDTO dto) {
        Anuncio anuncio = Anuncio.builder()
                .titulo(dto.getTitulo())
                .mensaje(dto.getMensaje())
                .verMasTexto(dto.getVerMasTexto() != null ? dto.getVerMasTexto() : "Ver más")
                .attachmentUrl(dto.getAttachmentUrl())
                .attachmentPublicId(dto.getAttachmentPublicId())
                .attachmentResourceType(dto.getAttachmentResourceType())
                .attachmentMimeType(dto.getAttachmentMimeType())
                .imageUrl(dto.getImageUrl())
                .imagePublicId(dto.getImagePublicId())
                .imageMimeType(dto.getImageMimeType())
                .mostrarEnPopup(true)
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .orden(anuncioRepository.findAllByOrderByOrdenAscIdAsc().stream().mapToInt(a -> a.getOrden() != null ? a.getOrden() : 0).findFirst().orElse(1) - 1)
                .expiresAt(dto.getExpiresAt() != null && !dto.getExpiresAt().trim().isEmpty() ? LocalDate.parse(dto.getExpiresAt()) : null)
                .build();
        return toDTO(anuncioRepository.save(anuncio));
    }

    @Transactional
    public AnuncioDTO update(Long id, AnuncioDTO dto) {
        Anuncio anuncio = findOrThrow(id);
        anuncio.setTitulo(dto.getTitulo());
        anuncio.setMensaje(dto.getMensaje());
        anuncio.setVerMasTexto(dto.getVerMasTexto() != null ? dto.getVerMasTexto() : anuncio.getVerMasTexto());
        anuncio.setAttachmentUrl(dto.getAttachmentUrl());
        anuncio.setAttachmentPublicId(dto.getAttachmentPublicId());
        anuncio.setAttachmentResourceType(dto.getAttachmentResourceType());
        anuncio.setAttachmentMimeType(dto.getAttachmentMimeType());
        anuncio.setImageUrl(dto.getImageUrl());
        anuncio.setImagePublicId(dto.getImagePublicId());
        anuncio.setImageMimeType(dto.getImageMimeType());
        anuncio.setExpiresAt(dto.getExpiresAt() != null && !dto.getExpiresAt().trim().isEmpty() ? LocalDate.parse(dto.getExpiresAt()) : null);
        if (dto.getActivo() != null) anuncio.setActivo(dto.getActivo());
        return toDTO(anuncioRepository.save(anuncio));
    }

    @Transactional
    public void delete(Long id) {
        hardDelete(id);
    }

    @Transactional
    public void hardDelete(Long id) {
        if (!anuncioRepository.existsById(id)) {
            throw new EntityNotFoundException("Anuncio no encontrado: " + id);
        }
        anuncioRepository.deleteById(id);
    }

    private Anuncio findOrThrow(Long id) {
        return anuncioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anuncio no encontrado: " + id));
    }

    private AnuncioDTO toDTO(Anuncio anuncio) {
        return AnuncioDTO.builder()
                .id(anuncio.getId())
                .titulo(anuncio.getTitulo())
                .mensaje(anuncio.getMensaje())
                .verMasTexto(anuncio.getVerMasTexto())
                .attachmentUrl(anuncio.getAttachmentUrl())
                .attachmentPublicId(anuncio.getAttachmentPublicId())
                .attachmentResourceType(anuncio.getAttachmentResourceType())
                .attachmentMimeType(anuncio.getAttachmentMimeType())
                .imageUrl(anuncio.getImageUrl())
                .imagePublicId(anuncio.getImagePublicId())
                .imageMimeType(anuncio.getImageMimeType())
                .mostrarEnPopup(anuncio.getMostrarEnPopup())
                .activo(anuncio.getActivo())
                .orden(anuncio.getOrden())
                .expiresAt(anuncio.getExpiresAt() != null ? anuncio.getExpiresAt().toString() : null)
                .build();
    }
}
