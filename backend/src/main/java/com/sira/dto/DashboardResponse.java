package com.sira.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor
public class DashboardResponse {
    private String semestreActivo;
    private long alumnosActivos;
    private long maestrosActivos;
    private long gruposEnCurso;
    private long inscripcionesActivas;
    private List<GrupoAlertaDto> gruposSinActividades;
    private List<GrupoAlertaDto> gruposPendientesCierre;
    private List<AlumnoAlertaDto> alumnosSinInscripciones;
}
