package com.sira.service;

import com.sira.model.Grupo;
import com.sira.model.Unidad;
import com.sira.repository.GrupoRepository;
import com.sira.repository.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UnidadService {

    @Autowired private UnidadRepository unidadRepository;
    @Autowired private GrupoRepository grupoRepository;

    @Transactional(readOnly = true)
    public List<Unidad> listarPorMateria(Integer materiaId) {
        return unidadRepository.findByMateriaIdOrderByNumeroAsc(materiaId);
    }

    @Transactional(readOnly = true)
    public List<Unidad> listarPorGrupo(Integer grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado con id: " + grupoId));
        return unidadRepository.findByMateriaIdOrderByNumeroAsc(grupo.getMateria().getId());
    }

    @Transactional
    public void actualizarNombre(Integer id, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la unidad no puede estar vacío.");
        }
        Unidad unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Unidad no encontrada con id: " + id));
        unidad.setNombre(nombre.trim());
        unidadRepository.save(unidad);
    }
}
