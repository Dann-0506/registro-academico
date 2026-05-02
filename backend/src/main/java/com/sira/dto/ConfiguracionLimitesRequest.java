package com.sira.dto;

import java.math.BigDecimal;

public record ConfiguracionLimitesRequest(BigDecimal minimaAprobatoria, BigDecimal maxima, String semestreActivo) {}
