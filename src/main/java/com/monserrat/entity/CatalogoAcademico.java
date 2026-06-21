package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "catalogos_academicos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_catalogo_academico_tipo_nivel_codigo", columnNames = {"tipo", "nivel", "codigo"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false, length = 20)
    private String nivel;

    @Column(nullable = false, length = 80)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;
}
