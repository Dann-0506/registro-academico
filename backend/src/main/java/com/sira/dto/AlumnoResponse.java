package com.sira.dto;

import com.sira.model.Alumno;

public record AlumnoResponse(Integer id, Integer usuarioId, String numControl, String nombre, String email, boolean activo) {

    public static AlumnoResponse from(Alumno a) {
        return new AlumnoResponse(
                a.getId(),
                a.getUsuario().getId(),
                a.getMatricula(),
                a.getUsuario().getNombre(),
                a.getUsuario().getEmail(),
                a.getUsuario().isActivo()
        );
    }
}
