package com.sira.dto;

import com.sira.model.Alumno;

public record AlumnoAlertaDto(Integer id, String numControl, String nombre, String email) {

    public static AlumnoAlertaDto from(Alumno a) {
        return new AlumnoAlertaDto(
                a.getId(),
                a.getMatricula(),
                a.getUsuario().getNombre(),
                a.getUsuario().getEmail()
        );
    }
}
