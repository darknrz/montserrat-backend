package com.monserrat.service;

import com.monserrat.dto.VideoDTO;
import com.monserrat.entity.Video;
import com.monserrat.repository.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    // ── GET públicos ─────────────────────────────────────────────────────────

    public List<VideoDTO> getAll() {
        return videoRepository.findByActivoTrueOrderByOrdenAsc()
                .stream().map(this::toDTO).toList();
    }

    public List<VideoDTO> getByTag(String tag) {
        return videoRepository.findByTagAndActivoTrue(tag)
                .stream().map(this::toDTO).toList();
    }

    public VideoDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    // ── CRUD admin ───────────────────────────────────────────────────────────

    @Transactional
    public VideoDTO create(VideoDTO dto) {
        Video video = Video.builder()
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .mediaType(dto.getMediaType())
                .mediaUrl(dto.getMediaUrl())
                .publicId(dto.getPublicId())
                .thumbnailUrl(dto.getThumbnailUrl())
                .formato(dto.getFormato())
                .tag(dto.getTag())
                .tagColor(dto.getTagColor())
                .orden(dto.getOrden() != null ? dto.getOrden() : 0)
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .creadoEn(LocalDateTime.now())
                .build();
        return toDTO(videoRepository.save(video));
    }

    @Transactional
    public VideoDTO update(Long id, VideoDTO dto) {
        Video video = findOrThrow(id);
        video.setTitulo(dto.getTitulo());
        video.setDescripcion(dto.getDescripcion());
        video.setMediaType(dto.getMediaType());
        video.setMediaUrl(dto.getMediaUrl());
        video.setPublicId(dto.getPublicId());
        video.setThumbnailUrl(dto.getThumbnailUrl());
        video.setFormato(dto.getFormato());
        video.setTag(dto.getTag());
        video.setTagColor(dto.getTagColor());
        if (dto.getOrden() != null) video.setOrden(dto.getOrden());
        if (dto.getActivo() != null) video.setActivo(dto.getActivo());
        return toDTO(videoRepository.save(video));
    }

    @Transactional
    public void delete(Long id) {
        Video video = findOrThrow(id);
        video.setActivo(false);
        videoRepository.save(video);
    }

    @Transactional
    public void hardDelete(Long id) {
        if (!videoRepository.existsById(id))
            throw new EntityNotFoundException("Video no encontrado: " + id);
        videoRepository.deleteById(id);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Video findOrThrow(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video no encontrado: " + id));
    }

    private VideoDTO toDTO(Video v) {
        return VideoDTO.builder()
                .id(v.getId())
                .titulo(v.getTitulo())
                .descripcion(v.getDescripcion())
                .mediaType(v.getMediaType())
                .mediaUrl(v.getMediaUrl())
                .publicId(v.getPublicId())
                .thumbnailUrl(v.getThumbnailUrl())
                .formato(v.getFormato())
                .tag(v.getTag())
                .tagColor(v.getTagColor())
                .orden(v.getOrden())
                .activo(v.getActivo())
                .build();
    }
}
