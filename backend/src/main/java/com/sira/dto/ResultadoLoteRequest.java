package com.sira.dto;

import java.util.List;

public record ResultadoLoteRequest(Integer grupoId, Integer unidadId, List<ResultadoItemRequest> resultados) {}
