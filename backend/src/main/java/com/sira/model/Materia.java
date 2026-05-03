package com.sira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "materia", uniqueConstraints = {
    @UniqueConstraint(columnNames = "clave")
})
@Getter @Setter @NoArgsConstructor
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clave", length = 20, nullable = false, unique = true)
    private String clave;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "total_unidades", nullable = false)
    private int totalUnidades;

    @OneToMany(mappedBy = "materia", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    private List<Unidad> unidades = new ArrayList<>();

    public Materia(String clave, String nombre, int totalUnidades) {
        this.clave = clave;
        this.nombre = nombre;
        this.totalUnidades = totalUnidades;
    }

    @Override
    public String toString() {
        return nombre + " (" + clave + ")";
    }
}
