package com.sira.dto;

import com.sira.model.Usuario;

public record PerfilResponse(
        Integer id,
        String nombre,
        String email,
        String rol,
        String identificador
) {
    public static PerfilResponse of(Usuario usuario, String identificador) {
        return new PerfilResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                identificador
        );
    }
}
