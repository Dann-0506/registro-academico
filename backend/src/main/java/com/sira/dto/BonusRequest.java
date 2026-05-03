package com.sira.dto;

import java.math.BigDecimal;

public record BonusRequest(Integer inscripcionId, Integer unidadId, String tipo, BigDecimal puntos, String justificacion) {}
