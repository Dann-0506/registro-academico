package com.sira.dto;

import java.math.BigDecimal;

public record ResultadoItemRequest(Integer inscripcionId, Integer actividadGrupoId, BigDecimal calificacion) {}
