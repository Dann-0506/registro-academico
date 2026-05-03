package com.sira.dto;

import com.sira.model.Bonus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BonusResponse(
        Integer id,
        Integer inscripcionId,
        Integer unidadId,
        String tipo,
        BigDecimal puntos,
        String justificacion,
        LocalDateTime otorgadoEn
) {
    public static BonusResponse from(Bonus b) {
        return new BonusResponse(
                b.getId(),
                b.getInscripcion().getId(),
                b.getUnidad() != null ? b.getUnidad().getId() : null,
                b.getTipo(),
                b.getPuntos(),
                b.getJustificacion(),
                b.getOtorgadoEn()
        );
    }
}
