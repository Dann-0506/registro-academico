package com.sira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "maestro", uniqueConstraints = {
    @UniqueConstraint(columnNames = "usuario_id"),
    @UniqueConstraint(columnNames = "num_empleado")
})
@Getter @Setter @NoArgsConstructor
public class Maestro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "num_empleado", length = 20, nullable = false, unique = true)
    private String numEmpleado;

    public Maestro(Usuario usuario, String numEmpleado) {
        this.usuario = usuario;
        this.numEmpleado = numEmpleado;
    }

    @Override
    public String toString() {
        return "[" + numEmpleado + "] " + (usuario != null ? usuario.getNombre() : "");
    }
}
