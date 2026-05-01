package com.sira.dto;

import java.time.LocalDate;

public record InscripcionRequest(Integer alumnoId, Integer grupoId, LocalDate fecha) {}
