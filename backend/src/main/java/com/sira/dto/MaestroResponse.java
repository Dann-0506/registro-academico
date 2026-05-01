package com.sira.dto;

import com.sira.model.Maestro;

public record MaestroResponse(Integer id, Integer usuarioId, String numEmpleado, String nombre, String email, boolean activo) {

    public static MaestroResponse from(Maestro m) {
        return new MaestroResponse(
                m.getId(),
                m.getUsuario().getId(),
                m.getNumEmpleado(),
                m.getUsuario().getNombre(),
                m.getUsuario().getEmail(),
                m.getUsuario().isActivo()
        );
    }
}
