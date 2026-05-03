package com.sira.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class ResultadoUnidadDto {

    private Integer inscripcionId;
    private Integer unidadId;
    private int unidadNumero;
    private String unidadNombre;
    private List<ResultadoDto> desglose;
    private BigDecimal resultadoBase;
    private BigDecimal bonusPuntos;
    private BigDecimal resultadoFinal;
    private int actividadesCalificadas;
    private int actividadesTotales;

    public String getEstado() {
        if (resultadoFinal == null) return "PENDIENTE";
        return actividadesCalificadas < actividadesTotales ? "PARCIAL" : "COMPLETO";
    }

    public boolean isPendiente() { return resultadoFinal == null; }
}
