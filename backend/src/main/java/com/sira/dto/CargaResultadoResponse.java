package com.sira.dto;

import java.util.List;

public record CargaResultadoResponse(int procesados, int exitosos, List<ErrorLinea> errores) {
    public record ErrorLinea(int linea, String mensaje) {}
}
