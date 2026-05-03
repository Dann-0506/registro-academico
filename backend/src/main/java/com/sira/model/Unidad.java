package com.sira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "unidad", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"materia_id", "numero"})
})
@Getter @Setter @NoArgsConstructor
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @Column(name = "numero", nullable = false)
    private int numero;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    public Unidad(Materia materia, int numero, String nombre) {
        this.materia = materia;
        this.numero = numero;
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Unidad " + numero + ": " + nombre;
    }
}
