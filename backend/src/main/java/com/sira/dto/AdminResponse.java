package com.sira.dto;

import com.sira.model.Administrador;

public record AdminResponse(Integer id, String numEmpleado, String nombre, String email, boolean activo) {

    public static AdminResponse from(Administrador a) {
        return new AdminResponse(
                a.getId(),
                a.getNumEmpleado(),
                a.getUsuario().getNombre(),
                a.getUsuario().getEmail(),
                a.getUsuario().isActivo()
        );
    }
}
