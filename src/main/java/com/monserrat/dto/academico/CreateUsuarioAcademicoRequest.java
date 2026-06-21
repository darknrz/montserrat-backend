package com.monserrat.dto.academico;

import com.monserrat.entity.EstadoMatricula;
import com.monserrat.entity.EstadoUsuario;
import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.Seccion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateUsuarioAcademicoRequest {
    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String nombres;
    private String apellidos;
    @Email(message = "El correo no es valido")
    private String correo;
    private String direccion;
    private LocalDate fechaNacimiento;
    private RolUsuario rol;

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
