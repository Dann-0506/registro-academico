package com.sira.dto;

public record MateriaReprobacionDto(
        Integer materiaId,
        String clave,
        String nombre,
        long gruposEvaluados,
        long totalAlumnos,
        long aprobados,
        long reprobados,
        double porcentajeReprobacion
) {}
