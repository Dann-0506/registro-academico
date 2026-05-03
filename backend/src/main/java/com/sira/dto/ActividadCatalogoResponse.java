package com.sira.dto;

import com.sira.model.ActividadCatalogo;

public record ActividadCatalogoResponse(Integer id, String nombre, String descripcion, boolean activo) {

    public static ActividadCatalogoResponse from(ActividadCatalogo a) {
        return new ActividadCatalogoResponse(a.getId(), a.getNombre(), a.getDescripcion(), a.isActivo());
    }
}
