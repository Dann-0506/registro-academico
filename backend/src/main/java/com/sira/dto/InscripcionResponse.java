package com.sira.dto;

import com.sira.model.Inscripcion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InscripcionResponse(
        Integer id,
        Integer alumnoId,
        String alumnoNombre,
        String alumnoNumControl,
        Integer grupoId,
        String grupoClave,
        String materiaNombre,
        String semestre,
        LocalDate fecha,
        String estadoAcademico,
        BigDecimal calificacionFinalCalculada,
        BigDecimal calificacionFinalOverride
) {
    public static InscripcionResponse from(Inscripcion i) {
        return new InscripcionResponse(
                i.getId(),
                i.getAlumno().getId(),
                i.getAlumno().getUsuario().getNombre(),
                i.getAlumno().getMatricula(),
                i.getGrupo().getId(),
                i.getGrupo().getClave(),
                i.getGrupo().getMateria().getNombre(),
                i.getGrupo().getSemestre(),
                i.getFecha(),
                i.getEstadoAcademico(),
                i.getCalificacionFinalCalculada(),
                i.getCalificacionFinalOverride()
        );
    }
}
