package com.sira.service;

import com.sira.model.ActividadCatalogo;
import com.sira.model.ActividadGrupo;
import com.sira.model.Grupo;
import com.sira.model.Unidad;
import com.sira.repository.ActividadCatalogoRepository;
import com.sira.repository.ActividadGrupoRepository;
import com.sira.repository.GrupoRepository;
import com.sira.repository.ResultadoRepository;
import com.sira.repository.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ActividadGrupoService {

    @Autowired private ActividadGrupoRepository actividadRepository;
    @Autowired private ActividadCatalogoRepository catalogoRepository;
    @Autowired private GrupoRepository grupoRepository;
    @Autowired private UnidadRepository unidadRepository;
    @Autowired private ResultadoRepository resultadoRepository;

    @Transactional(readOnly = true)
    public List<ActividadGrupo> listarPorGrupo(Integer grupoId) {
        return actividadRepository.findByGrupoIdWithUnidad(grupoId);
    }

    @Transactional(readOnly = true)
    public List<ActividadGrupo> listarPorGrupoYUnidad(Integer grupoId, Integer unidadId) {
        return actividadRepository.findByGrupoIdAndUnidadId(grupoId, unidadId);
    }

    @Transactional
    public ActividadGrupo crear(Integer grupoId, Integer unidadId,
                                Integer actividadCatalogoId, String etiqueta,
                                BigDecimal ponderacion) {
        validarPonderacion(ponderacion);

        ActividadCatalogo catalogo = catalogoRepository.findById(actividadCatalogoId)
                .orElseThrow(() -> new NoSuchElementException("Actividad del catálogo no encontrada: " + actividadCatalogoId));
        if (!catalogo.isActivo()) {
            throw new IllegalStateException("La actividad '" + catalogo.getNombre() + "' está desactivada y no puede usarse.");
        }

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado: " + grupoId));
        if (grupo.isCerrado()) {
            throw new IllegalStateException("No se pueden agregar actividades a un grupo cerrado.");
        }
        Unidad unidad = unidadRepository.findById(unidadId)
                .orElseThrow(() -> new NoSuchElementException("Unidad no encontrada: " + unidadId));

        BigDecimal sumActual = actividadRepository.sumPonderacionByGrupoIdAndUnidadId(grupoId, unidadId);
        if (sumActual.add(ponderacion).compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(
                    "La ponderación excede el 100%. Disponible: " + new BigDecimal("100").subtract(sumActual) + "%");
        }

        return actividadRepository.save(new ActividadGrupo(grupo, unidad, catalogo, etiqueta, ponderacion));
    }

    @Transactional
    public ActividadGrupo actualizar(Integer id, String etiqueta, BigDecimal ponderacion) {
        validarPonderacion(ponderacion);
        ActividadGrupo actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Actividad no encontrada: " + id));

        BigDecimal sumSinEsta = actividadRepository
                .sumPonderacionByGrupoIdAndUnidadId(actividad.getGrupo().getId(), actividad.getUnidad().getId())
                .subtract(actividad.getPonderacion());

        if (sumSinEsta.add(ponderacion).compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(
                    "La ponderación excede el 100%. Disponible: " + new BigDecimal("100").subtract(sumSinEsta) + "%");
        }

        actividad.setEtiqueta(etiqueta != null && !etiqueta.isBlank() ? etiqueta.trim() : null);
        actividad.setPonderacion(ponderacion);
        // Actualiza nombre legado para consistencia
        if (actividad.getActividadCatalogo() != null) {
            actividad.setNombre(actividad.getNombreCompleto());
        }
        return actividadRepository.save(actividad);
    }

    @Transactional
    public void eliminar(Integer id) {
        ActividadGrupo actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Actividad no encontrada: " + id));
        if (resultadoRepository.tieneCalificacionesRegistradas(id)) {
            throw new IllegalStateException("No se puede eliminar: la actividad ya tiene calificaciones registradas.");
        }
        actividadRepository.delete(actividad);
    }

    private void validarPonderacion(BigDecimal ponderacion) {
        if (ponderacion == null || ponderacion.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La ponderación debe ser mayor a 0.");
        }
        if (ponderacion.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("La ponderación no puede superar el 100%.");
        }
    }
}
