package com.monserrat.service;

import com.monserrat.dto.VideoDTO;
import com.monserrat.entity.Video;
import com.monserrat.repository.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoService videoService;

    @Test
    void createDefaultsOrdenAndActivoAndSetsCreationDate() {
        VideoDTO dto = VideoDTO.builder()
                .titulo("Ceremonia")
                .descripcion("Video institucional")
                .mediaType("video")
                .mediaUrl("https://example.test/video.mp4")
                .publicId("videos/ceremonia")
                .thumbnailUrl("https://example.test/thumb.jpg")
                .formato("mp4")
                .tag("Eventos")
                .tagColor("#1f2937")
                .build();

        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        VideoDTO result = videoService.create(dto);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(captor.capture());
        assertThat(captor.getValue().getOrden()).isZero();
        assertThat(captor.getValue().getActivo()).isTrue();
        assertThat(captor.getValue().getCreadoEn()).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
    }

    @Test
    void updateKeepsExistingOrdenAndActivoWhenDtoOmitsThem() {
        Video existing = video(7L);
        existing.setOrden(12);
        existing.setActivo(false);
        when(videoRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(videoRepository.save(existing)).thenReturn(existing);

        VideoDTO dto = VideoDTO.builder()
                .titulo("Nuevo titulo")
                .descripcion("Nueva descripcion")
                .mediaType("image")
                .mediaUrl("https://example.test/image.jpg")
                .publicId("images/nueva")
                .thumbnailUrl("https://example.test/thumb.jpg")
                .formato("jpg")
                .tag("Galeria")
                .tagColor("#0f766e")
                .build();

        VideoDTO result = videoService.update(7L, dto);

        assertThat(existing.getOrden()).isEqualTo(12);
        assertThat(existing.getActivo()).isFalse();
        assertThat(result.getTitulo()).isEqualTo("Nuevo titulo");
        assertThat(result.getOrden()).isEqualTo(12);
        verify(videoRepository).save(existing);
    }

    @Test
    void hardDeleteThrowsWhenVideoDoesNotExist() {
        when(videoRepository.existsById(55L)).thenReturn(false);

        assertThatThrownBy(() -> videoService.hardDelete(55L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Video no encontrado: 55");
    }

    private static Video video(Long id) {
        return Video.builder()
                .id(id)
                .titulo("Ceremonia")
                .descripcion("Video institucional")
                .mediaType("video")
                .mediaUrl("https://example.test/video.mp4")
                .publicId("videos/ceremonia")
                .thumbnailUrl("https://example.test/thumb.jpg")
                .formato("mp4")
                .tag("Eventos")
                .tagColor("#1f2937")
                .orden(1)
                .activo(true)
                .build();
    }
}
