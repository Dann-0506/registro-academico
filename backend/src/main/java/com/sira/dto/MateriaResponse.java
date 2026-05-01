package com.sira.dto;

import com.sira.model.Materia;
import com.sira.model.Unidad;

import java.util.List;

public record MateriaResponse(Integer id, String clave, String nombre, int totalUnidades, List<UnidadDto> unidades) {

    public record UnidadDto(Integer id, int numero, String nombre) {
        public static UnidadDto from(Unidad u) {
            return new UnidadDto(u.getId(), u.getNumero(), u.getNombre());
        }
    }

    public static MateriaResponse from(Materia m) {
        List<UnidadDto> unidadesDto = m.getUnidades().stream()
                .map(UnidadDto::from)
                .toList();
        return new MateriaResponse(m.getId(), m.getClave(), m.getNombre(), m.getTotalUnidades(), unidadesDto);
    }
}
