package com.monserrat.dto.anio;

import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.Seccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigracionItemDTO {
    private Long alumnoId;
    private String dni;
    private String codigo;
    private String nombre;
    private NivelEducativo nivelActual;
    private Grado gradoActual;
    private Seccion seccionActual;
    private AccionMigracion accion;
    private NivelEducativo nivelDestino;
    private Grado gradoDestino;
    private Seccion seccionDestino;
    private List<Seccion> seccionesPermitidas;
    /** true cuando falta elegir la sección de destino para poder ejecutar. */
    private boolean requiereSeccion;
    /** true si el grado de destino tiene grupos (Ciclado, Anual, Letras, Ciencias); si no, no hay nada que elegir. */
    private boolean usaGrupo;
    /** true si el salón de destino lo propuso el sistema por la escalera académica (conviene revisarlo). */
    private boolean salonSugerido;
}
