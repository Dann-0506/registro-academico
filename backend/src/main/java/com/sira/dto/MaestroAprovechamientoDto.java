package com.sira.dto;

public record MaestroAprovechamientoDto(
        Integer maestroId,
        String nombre,
        String numEmpleado,
        long grupos,
        long alumnosEvaluados,
        long aprobados,
        long reprobados,
        double porcentajeAprobacion
) {}
