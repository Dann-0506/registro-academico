package com.sira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "actividad_grupo")
@Getter @Setter @NoArgsConstructor
public class ActividadGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    // Referencia al catálogo (nullable para compatibilidad con datos legados)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_catalogo_id")
    private ActividadCatalogo actividadCatalogo;

    // Etiqueta opcional del maestro para distinguir múltiples instancias (ej. "1er parcial")
    @Column(name = "etiqueta", length = 100)
    private String etiqueta;

    // Mantenido para datos legados (actividades creadas antes del catálogo)
    @Column(name = "nombre", length = 150)
    private String nombre;

    @Column(name = "ponderacion", precision = 5, scale = 2, nullable = false)
    private BigDecimal ponderacion;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    public ActividadGrupo(Grupo grupo, Unidad unidad, ActividadCatalogo catalogo, String etiqueta, BigDecimal ponderacion) {
        this.grupo = grupo;
        this.unidad = unidad;
        this.actividadCatalogo = catalogo;
        this.etiqueta = etiqueta;
        // Establece nombre legado igual al nombre del catálogo para compatibilidad
        this.nombre = catalogo.getNombre();
        this.ponderacion = ponderacion;
    }

    public String getNombreCompleto() {
        if (actividadCatalogo != null) {
            String base = actividadCatalogo.getNombre();
            return (etiqueta != null && !etiqueta.isBlank()) ? base + " — " + etiqueta.trim() : base;
        }
        return nombre != null ? nombre : "";
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + ponderacion + "%)";
    }
}
