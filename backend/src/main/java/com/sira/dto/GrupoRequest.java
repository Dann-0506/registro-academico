package com.sira.dto;

import java.math.BigDecimal;

public record GrupoRequest(
        Integer materiaId,
        Integer maestroId,
        String clave,
        String semestre,
        BigDecimal calificacionMinimaAprobatoria,
        BigDecimal calificacionMaxima
) {}
