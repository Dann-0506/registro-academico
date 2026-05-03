package com.sira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "estado_unidad", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"grupo_id", "unidad_id"})
})
@Getter @Setter @NoArgsConstructor
public class EstadoUnidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado = "ABIERTA";

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public EstadoUnidad(Grupo grupo, Unidad unidad) {
        this.grupo = grupo;
        this.unidad = unidad;
    }

    public boolean isCerrada() {
        return "CERRADA".equals(estado);
    }
}
