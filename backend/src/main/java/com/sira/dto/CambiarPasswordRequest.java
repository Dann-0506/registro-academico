package com.sira.dto;

public record CambiarPasswordRequest(String passwordActual, String passwordNueva, String passwordConfirmar) {}
