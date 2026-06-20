package com.monserrat.dto.academico;

import com.monserrat.entity.EstadoMatricula;
import com.monserrat.entity.EstadoUsuario;
import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.Seccion;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePerfilAcademicoRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String codigo;
    private String nombres;
    private String apellidos;
    private String correo;
    private String direccion;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String fotoUrl;
    private NivelEducativo nivelEducativo;
    private Grado grado;
    private Seccion seccion;
    private String materia;
    private String especialidad;
    private EstadoUsuario estado;
    private EstadoMatricula estadoMatricula;
    private Boolean pensionPagada;
    private String pensionObservacion;
}
