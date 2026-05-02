package com.sira.dto;

import com.sira.model.ActividadGrupo;

import java.math.BigDecimal;

public record ActividadGrupoResponse(
        Integer id,
        Integer grupoId,
        Integer unidadId,
        int unidadNumero,
        String unidadNombre,
        String nombre,
        BigDecimal ponderacion,
        Integer actividadCatalogoId,
        String etiqueta
) {
    public static ActividadGrupoResponse from(ActividadGrupo a) {
        return new ActividadGrupoResponse(
                a.getId(),
                a.getGrupo().getId(),
                a.getUnidad().getId(),
                a.getUnidad().getNumero(),
                a.getUnidad().getNombre(),
                a.getNombreCompleto(),
                a.getPonderacion(),
                a.getActividadCatalogo() != null ? a.getActividadCatalogo().getId() : null,
                a.getEtiqueta()
        );
    }
}
