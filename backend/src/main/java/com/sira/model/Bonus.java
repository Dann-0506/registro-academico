package com.sira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bonus")
@Getter @Setter @NoArgsConstructor
public class Bonus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    @Column(name = "tipo", length = 10, nullable = false)
    private String tipo;

    @Column(name = "puntos", precision = 5, scale = 2, nullable = false)
    private BigDecimal puntos;

    @Column(name = "justificacion", columnDefinition = "TEXT")
    private String justificacion;

    @CreationTimestamp
    @Column(name = "otorgado_en", updatable = false)
    private LocalDateTime otorgadoEn;

    public Bonus(Inscripcion inscripcion, Unidad unidad, String tipo, BigDecimal puntos, String justificacion) {
        this.inscripcion = inscripcion;
        this.unidad = unidad;
        this.tipo = tipo;
        this.puntos = puntos;
        this.justificacion = justificacion;
    }

    public boolean esDeUnidad() { return "unidad".equals(tipo); }
    public boolean esDeMateria() { return "materia".equals(tipo); }

    @Override
    public String toString() {
        return "+" + puntos + " pts (" + tipo + ")";
    }
}
