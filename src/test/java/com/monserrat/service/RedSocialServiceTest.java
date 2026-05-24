package com.monserrat.service;

import com.monserrat.dto.RedSocialDTO;
import com.monserrat.entity.RedSocial;
import com.monserrat.repository.RedSocialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedSocialServiceTest {

    @Mock
    private RedSocialRepository redSocialRepository;

    @InjectMocks
    private RedSocialService redSocialService;

    @Test
    void createDefaultsOrdenAndActivo() {
        RedSocialDTO dto = RedSocialDTO.builder()
                .nombre("Facebook")
                .icono("facebook")
                .url("https://facebook.test/monserrat")
                .build();

        when(redSocialRepository.save(any(RedSocial.class))).thenAnswer(invocation -> {
            RedSocial saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        RedSocialDTO result = redSocialService.create(dto);

        ArgumentCaptor<RedSocial> captor = ArgumentCaptor.forClass(RedSocial.class);
        verify(redSocialRepository).save(captor.capture());
        assertThat(captor.getValue().getOrden()).isZero();
        assertThat(captor.getValue().getActivo()).isTrue();
        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void updateKeepsExistingOrdenAndActivoWhenDtoOmitsThem() {
        RedSocial existing = RedSocial.builder()
                .id(4L)
                .nombre("Facebook")
                .icono("facebook")
                .url("https://facebook.test/old")
                .orden(3)
                .activo(false)
                .build();
        when(redSocialRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(redSocialRepository.save(existing)).thenReturn(existing);

        RedSocialDTO dto = RedSocialDTO.builder()
                .nombre("Instagram")
                .icono("instagram")
                .url("https://instagram.test/monserrat")
                .build();

        RedSocialDTO result = redSocialService.update(4L, dto);

        assertThat(existing.getOrden()).isEqualTo(3);
        assertThat(existing.getActivo()).isFalse();
        assertThat(result.getNombre()).isEqualTo("Instagram");
        assertThat(result.getOrden()).isEqualTo(3);
        verify(redSocialRepository).save(existing);
    }
}
