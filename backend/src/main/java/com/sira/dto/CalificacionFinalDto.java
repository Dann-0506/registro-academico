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

    public String getEstado() {
        if (calificacionFinal == null) return "PENDIENTE";
        return "APROBADO"; // el estado real lo determina CalificacionService con el límite
    }

    public boolean isPendiente() { return calificacionFinal == null; }
}
