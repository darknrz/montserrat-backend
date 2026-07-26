package com.monserrat.dto.academico;

import com.monserrat.entity.EstadoMatricula;
import com.monserrat.entity.EstadoUsuario;
import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.Seccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilAcademicoDTO {
    private Long id;
    private String dni;
    private String codigo;
    private String codigoChatbot;
    private String nombre;
    private String nombres;
    private String apellidos;
    private String correo;
    private String direccion;
    private LocalDate fechaNacimiento;
    private RolUsuario rol;
    private Boolean activo;
    private EstadoUsuario estado;
    private Boolean debeCambiarContrasena;
    private String telefono;
    private String fotoUrl;
    private NivelEducativo nivelEducativo;
    private Grado grado;
    private Seccion seccion;
    private String materia;
    private String especialidad;
    private EstadoMatricula estadoMatricula;
    private Boolean pensionPagada;
    private String pensionObservacion;
    private LocalDateTime createdAt;
    private LocalDateTime inicioPeriodo;
}
