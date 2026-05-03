package com.sira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "grupo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"clave", "materia_id", "semestre"})
})
@Getter @Setter @NoArgsConstructor
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maestro_id", nullable = false)
    private Maestro maestro;

    @Column(name = "clave", length = 20, nullable = false)
    private String clave;

    @Column(name = "semestre", length = 50, nullable = false)
    private String semestre;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "estado_evaluacion", length = 20, nullable = false)
    private String estadoEvaluacion = "ABIERTO";

    @Column(name = "calificacion_minima_aprobatoria", precision = 5, scale = 2)
    private BigDecimal calificacionMinimaAprobatoria = new BigDecimal("70.00");

    @Column(name = "calificacion_maxima", precision = 5, scale = 2)
    private BigDecimal calificacionMaxima = new BigDecimal("100.00");

    public Grupo(Materia materia, Maestro maestro, String clave, String semestre) {
        this.materia = materia;
        this.maestro = maestro;
        this.clave = clave;
        this.semestre = semestre;
    }

    public boolean isCerrado() {
        return "CERRADO".equals(estadoEvaluacion);
    }

    @Override
    public String toString() {
        return "[" + clave + "] " + (materia != null ? materia.getNombre() : "") + " (" + semestre + ")";
    }
}
