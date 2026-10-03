package com.monserrat.service;

import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.Seccion;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class MigracionAnioServiceTest {

    @Test
    void siguienteGradoAvanzaUnoYTerminaEnQuintoDeSecundaria() {
        assertThat(MigracionAnioService.siguienteGrado(Grado.INICIAL)).isEqualTo(Grado.PRIMERO_PRIMARIA);
        assertThat(MigracionAnioService.siguienteGrado(Grado.SEXTO_PRIMARIA)).isEqualTo(Grado.PRIMERO_SECUNDARIA);
        assertThat(MigracionAnioService.siguienteGrado(Grado.CUARTO_SECUNDARIA)).isEqualTo(Grado.QUINTO_SECUNDARIA);
        assertThat(MigracionAnioService.siguienteGrado(Grado.QUINTO_SECUNDARIA)).isNull();
    }

    @Test
    void nivelSeDerivaDelGrado() {
        assertThat(MigracionAnioService.nivelDeGrado(Grado.INICIAL)).isEqualTo(NivelEducativo.INICIAL);
        assertThat(MigracionAnioService.nivelDeGrado(Grado.SEXTO_PRIMARIA)).isEqualTo(NivelEducativo.PRIMARIA);
        assertThat(MigracionAnioService.nivelDeGrado(Grado.PRIMERO_SECUNDARIA)).isEqualTo(NivelEducativo.SECUNDARIA);
    }

    @Test
    void seccionesPermitidasRespetanGruposPorGrado() {
        assertThat(MigracionAnioService.seccionesPermitidas(Grado.SEGUNDO_PRIMARIA))
                .containsExactly(Seccion.A, Seccion.B, Seccion.C, Seccion.D);
        assertThat(MigracionAnioService.seccionesPermitidas(Grado.SEXTO_PRIMARIA))
                .containsExactly(Seccion.CICLADO_I, Seccion.CICLADO_II);
        assertThat(MigracionAnioService.seccionesPermitidas(Grado.QUINTO_SECUNDARIA))
                .containsExactly(Seccion.LETRAS, Seccion.CIENCIAS);
    }

    @Test
    void soloLosGradosConGrupoPidenDecision() {
        assertThat(MigracionAnioService.usaGrupo(Grado.TERCERO_PRIMARIA)).isFalse();
        assertThat(MigracionAnioService.usaGrupo(Grado.SEGUNDO_SECUNDARIA)).isTrue(); // solo ANUAL
        assertThat(MigracionAnioService.seccionesPermitidas(Grado.SEGUNDO_SECUNDARIA)).containsExactly(Seccion.ANUAL);
        assertThat(MigracionAnioService.usaGrupo(Grado.SEXTO_PRIMARIA)).isTrue();
        assertThat(MigracionAnioService.usaGrupo(Grado.QUINTO_SECUNDARIA)).isTrue();
    }

    @Test
    void salonSugeridoSigueLaEscaleraAcademica() {
        // 5to primaria (sin salón) entra al primer peldaño de 6to: Ciclado I
        assertThat(MigracionAnioService.sugerirSalon(Seccion.A, MigracionAnioService.seccionesPermitidas(Grado.SEXTO_PRIMARIA)))
                .isEqualTo(Seccion.CICLADO_I);
        // 2do secundaria (sin salón) entra a Anual en 3ro
        assertThat(MigracionAnioService.sugerirSalon(null, MigracionAnioService.seccionesPermitidas(Grado.TERCERO_SECUNDARIA)))
                .isEqualTo(Seccion.ANUAL);
        // Anual -> 4to: Letras y Ciencias empatan, lo decide el admin
        assertThat(MigracionAnioService.sugerirSalon(Seccion.ANUAL, MigracionAnioService.seccionesPermitidas(Grado.CUARTO_SECUNDARIA)))
                .isNull();
        // Ciclado I -> Ciclado II si el destino lo admite y el siguiente peldaño es único
        assertThat(MigracionAnioService.sugerirSalon(Seccion.CICLADO_I, MigracionAnioService.seccionesPermitidas(Grado.PRIMERO_SECUNDARIA)))
                .isEqualTo(Seccion.CICLADO_II);
    }

    @Test
    void todosLosGradosTienenSeccionesPermitidas() {
        Arrays.stream(Grado.values())
                .forEach(g -> assertThat(MigracionAnioService.seccionesPermitidas(g)).isNotEmpty());
    }
}
