package com.sira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inscripcion", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"alumno_id", "grupo_id"})
})
@Getter @Setter @NoArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "calificacion_final_calculada", precision = 5, scale = 2)
    private BigDecimal calificacionFinalCalculada;

    @Column(name = "estado_academico", length = 20, nullable = false)
    private String estadoAcademico = "PENDIENTE";

    @Column(name = "calificacion_final_override", precision = 5, scale = 2)
    private BigDecimal calificacionFinalOverride;

    @Column(name = "override_justificacion", columnDefinition = "TEXT")
    private String overrideJustificacion;

    public Inscripcion(Alumno alumno, Grupo grupo, LocalDate fecha) {
        this.alumno = alumno;
        this.grupo = grupo;
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Inscripción Alumno " + alumno.getId() + " en Grupo " + grupo.getId();
    }
}
