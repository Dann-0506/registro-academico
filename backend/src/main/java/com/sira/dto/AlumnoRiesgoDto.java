package com.sira.dto;

import java.util.List;

public record AlumnoRiesgoDto(
        Integer alumnoId,
        String numControl,
        String nombre,
        String email,
        int materiasReprobadas,
        List<String> grupos
) {}
