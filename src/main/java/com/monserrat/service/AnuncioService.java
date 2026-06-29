package com.monserrat.service;

import com.monserrat.dto.AnuncioDTO;
import com.monserrat.entity.Anuncio;
import com.monserrat.repository.AnuncioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnuncioService {

    private final AnuncioRepository anuncioRepository;

    public List<AnuncioDTO> getAllActive() {
        return anuncioRepository.findByActivoTrueOrderByOrdenAsc().stream().map(this::toDTO).toList();
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
                .mostrarEnPopup(dto.getMostrarEnPopup() != null ? dto.getMostrarEnPopup() : true)
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .orden(dto.getOrden() != null ? dto.getOrden() : 0)
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
        if (dto.getMostrarEnPopup() != null) anuncio.setMostrarEnPopup(dto.getMostrarEnPopup());
        if (dto.getActivo() != null) anuncio.setActivo(dto.getActivo());
        if (dto.getOrden() != null) anuncio.setOrden(dto.getOrden());
        return toDTO(anuncioRepository.save(anuncio));
    }

    @Transactional
    public void delete(Long id) {
        Anuncio anuncio = findOrThrow(id);
        anuncio.setActivo(false);
        anuncioRepository.save(anuncio);
    }

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
                .mostrarEnPopup(anuncio.getMostrarEnPopup())
                .activo(anuncio.getActivo())
                .orden(anuncio.getOrden())
                .build();
    }
}
