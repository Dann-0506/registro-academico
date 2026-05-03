package com.sira.dto;

import java.math.BigDecimal;

public record ActividadGrupoRequest(Integer unidadId, Integer actividadCatalogoId, String etiqueta, BigDecimal ponderacion) {}
