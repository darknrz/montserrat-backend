package com.monserrat.repository;

import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.Seccion;
import com.monserrat.entity.UsuarioAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioAcademicoRepository extends JpaRepository<UsuarioAcademico, Long> {
    Optional<UsuarioAcademico> findByDni(String dni);
    boolean existsByDni(String dni);
    boolean existsByDniAndIdNot(String dni, Long id);
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
    List<UsuarioAcademico> findByRolAndActivoTrue(RolUsuario rol);
    List<UsuarioAcademico> findByRolAndNivelEducativoAndGradoAndSeccionAndActivoTrue(
            RolUsuario rol,
            NivelEducativo nivelEducativo,
            Grado grado,
            Seccion seccion);
}
