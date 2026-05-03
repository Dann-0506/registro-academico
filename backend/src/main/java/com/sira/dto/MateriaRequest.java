package com.sira.dto;

import java.util.List;

public record MateriaRequest(String clave, String nombre, int totalUnidades, List<String> nombresUnidades) {}
