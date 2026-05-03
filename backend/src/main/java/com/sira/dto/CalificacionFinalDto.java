package com.sira.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class CalificacionFinalDto {

    private Integer inscripcionId;
    private Integer alumnoId;
    private String alumnoNombre;
    private String alumnoNumControl;
    private List<ResultadoUnidadDto> unidades;
    private BigDecimal calificacionCalculada;
    private BigDecimal bonusMateria;
    private BigDecimal calificacionConBonus;
    private BigDecimal calificacionFinal;
    private boolean esOverride;
    private String overrideJustificacion;
    private String estado;

    public boolean isPendiente() { return "PENDIENTE".equals(estado); }
}
