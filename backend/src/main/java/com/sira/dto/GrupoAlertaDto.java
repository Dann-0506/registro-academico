package com.sira.dto;

import com.sira.model.Grupo;

public record GrupoAlertaDto(
        Integer id,
        String clave,
        String semestre,
        String materiaNombre,
        String maestroNombre
) {
    public static GrupoAlertaDto from(Grupo g) {
        return new GrupoAlertaDto(
                g.getId(),
                g.getClave(),
                g.getSemestre(),
                g.getMateria().getNombre(),
                g.getMaestro().getUsuario().getNombre()
        );
    }
}
