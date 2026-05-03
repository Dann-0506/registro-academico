package com.sira.service;

import com.sira.model.ActividadCatalogo;
import com.sira.repository.ActividadCatalogoRepository;
import com.sira.repository.ActividadGrupoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ActividadCatalogoService {

    @Autowired private ActividadCatalogoRepository catalogoRepository;
    @Autowired private ActividadGrupoRepository actividadGrupoRepository;

    @Transactional(readOnly = true)
    public List<ActividadCatalogo> listarTodas() {
        return catalogoRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<ActividadCatalogo> listarActivas() {
        return catalogoRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional
    public ActividadCatalogo crear(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la actividad es obligatorio.");
        }
        if (catalogoRepository.existsByNombre(nombre.trim())) {
            throw new IllegalStateException("Ya existe una actividad con el nombre '" + nombre.trim() + "'.");
        }
        return catalogoRepository.save(new ActividadCatalogo(nombre.trim(), descripcion));
    }

    @Transactional
    public ActividadCatalogo actualizar(Integer id, String nombre, String descripcion) {
        ActividadCatalogo cat = buscarPorId(id);
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la actividad es obligatorio.");
        }
        if (!cat.getNombre().equals(nombre.trim()) && catalogoRepository.existsByNombre(nombre.trim())) {
            throw new IllegalStateException("Ya existe una actividad con el nombre '" + nombre.trim() + "'.");
        }
        cat.setNombre(nombre.trim());
        cat.setDescripcion(descripcion);
        return catalogoRepository.save(cat);
    }

    @Transactional
    public void cambiarEstado(Integer id, boolean activo) {
        ActividadCatalogo cat = buscarPorId(id);
        cat.setActivo(activo);
        catalogoRepository.save(cat);
    }

    @Transactional
    public void eliminar(Integer id) {
        buscarPorId(id);
        if (actividadGrupoRepository.existsByActividadCatalogoId(id)) {
            throw new IllegalStateException(
                "No se puede eliminar: esta actividad está en uso en uno o más grupos. Usa 'Desactivar'.");
        }
        catalogoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ActividadCatalogo buscarPorId(Integer id) {
        return catalogoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Actividad del catálogo no encontrada con id: " + id));
    }
}
