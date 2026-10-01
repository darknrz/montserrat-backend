package com.monserrat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Taller fijo y unico (nombre + monto) por anio. Se aplica automaticamente a
 * todos los alumnos de los salones/grados indicados en aplicaA.
 * Tokens: "SALON:CICLADO I" o "GRADO:SEGUNDO_SECUNDARIA".
 */
@Entity
@Table(name = "taller_catalogo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TallerCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private BigDecimal monto;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "taller_catalogo_aplica", joinColumns = @JoinColumn(name = "catalogo_id"))
    @Column(name = "destino", length = 60)
    @Builder.Default
    private Set<String> aplicaA = new LinkedHashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
