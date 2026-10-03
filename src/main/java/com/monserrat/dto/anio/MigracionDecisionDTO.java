package com.monserrat.dto.anio;

import com.monserrat.entity.Seccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Decisión del admin para un alumno concreto. Los alumnos sin decisión usan la acción por defecto. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MigracionDecisionDTO {
    private Long alumnoId;
    private AccionMigracion accion;
    /** Sección o grupo de destino. Obligatorio si la actual no es válida en el grado de destino. */
    private Seccion seccion;
}
