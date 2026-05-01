package com.sira.dto;

import com.sira.model.Usuario;

public record AdminResponse(Integer id, String nombre, String email, boolean activo) {

    public static AdminResponse from(Usuario u) {
        return new AdminResponse(u.getId(), u.getNombre(), u.getEmail(), u.isActivo());
    }
}
