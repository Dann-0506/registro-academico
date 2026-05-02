package com.sira.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor
public class ReportesResponse {
    private String semestre;
    private List<MateriaReprobacionDto> materiasReprobacion;
    private List<AlumnoRiesgoDto> alumnosRiesgo;
    private List<MaestroAprovechamientoDto> maestrosAprovechamiento;
}
