package com.sira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultado", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"inscripcion_id", "actividad_grupo_id"})
})
@Getter @Setter @NoArgsConstructor
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actividad_grupo_id", nullable = false)
    private ActividadGrupo actividadGrupo;

    @Column(name = "calificacion", precision = 5, scale = 2)
    private BigDecimal calificacion;

    @UpdateTimestamp
    @Column(name = "modificado_en")
    private LocalDateTime modificadoEn;

    public Resultado(Inscripcion inscripcion, ActividadGrupo actividadGrupo, BigDecimal calificacion) {
        this.inscripcion = inscripcion;
        this.actividadGrupo = actividadGrupo;
        this.calificacion = calificacion;
    }

    public BigDecimal getAportacion() {
        BigDecimal pond = actividadGrupo != null ? actividadGrupo.getPonderacion() : null;
        if (calificacion == null || pond == null) return BigDecimal.ZERO;
        return calificacion.multiply(pond).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        String nombre = actividadGrupo != null ? actividadGrupo.getNombre() : "Actividad";
        return nombre + ": " + (calificacion != null ? calificacion : "Pte.");
    }
}
