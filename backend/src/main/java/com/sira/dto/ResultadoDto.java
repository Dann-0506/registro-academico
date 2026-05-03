package com.sira.dto;

import com.sira.model.Resultado;

import java.math.BigDecimal;

public record ResultadoDto(
        Integer id,
        Integer inscripcionId,
        Integer actividadGrupoId,
        String actividadNombre,
        BigDecimal ponderacion,
        BigDecimal calificacion,
        BigDecimal aportacion
) {
    public static ResultadoDto from(Resultado r) {
        return new ResultadoDto(
                r.getId(),
                r.getInscripcion().getId(),
                r.getActividadGrupo().getId(),
                r.getActividadGrupo().getNombreCompleto(),
                r.getActividadGrupo().getPonderacion(),
                r.getCalificacion(),
                r.getAportacion()
        );
    }
}
