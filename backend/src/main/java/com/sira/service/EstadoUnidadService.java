package com.sira.service;

import com.sira.model.EstadoUnidad;
import com.sira.model.Grupo;
import com.sira.model.Unidad;
import com.sira.repository.EstadoUnidadRepository;
import com.sira.repository.GrupoRepository;
import com.sira.repository.ResultadoRepository;
import com.sira.repository.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EstadoUnidadService {

    @Autowired private EstadoUnidadRepository estadoUnidadRepository;
    @Autowired private GrupoRepository grupoRepository;
    @Autowired private UnidadRepository unidadRepository;
    @Autowired private ResultadoRepository resultadoRepository;

    @Transactional(readOnly = true)
    public Optional<EstadoUnidad> obtenerEstado(Integer grupoId, Integer unidadId) {
        return estadoUnidadRepository.findByGrupoIdAndUnidadId(grupoId, unidadId);
    }

    public void validarUnidadAbierta(Integer grupoId, Integer unidadId) {
        estadoUnidadRepository.findByGrupoIdAndUnidadId(grupoId, unidadId)
                .filter(EstadoUnidad::isCerrada)
                .ifPresent(e -> { throw new IllegalStateException("La unidad ya ha sido cerrada."); });
    }

    @Transactional
    public void guardarEstado(Integer grupoId, Integer unidadId, String estado) {
        Optional<EstadoUnidad> existing = estadoUnidadRepository.findByGrupoIdAndUnidadId(grupoId, unidadId);
        if (existing.isPresent()) {
            estadoUnidadRepository.actualizarEstado(grupoId, unidadId, estado);
        } else {
            Grupo grupo = grupoRepository.findById(grupoId)
                    .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado: " + grupoId));
            Unidad unidad = unidadRepository.findById(unidadId)
                    .orElseThrow(() -> new NoSuchElementException("Unidad no encontrada: " + unidadId));
            EstadoUnidad eu = new EstadoUnidad(grupo, unidad);
            eu.setEstado(estado);
            estadoUnidadRepository.save(eu);
        }
    }

    @Transactional
    public void cerrarUnidad(Integer grupoId, Integer unidadId) {
        long pendientes = resultadoRepository.countAlumnosSinCalificarEnUnidad(grupoId, unidadId);
        if (pendientes > 0) {
            throw new IllegalStateException(
                    "No se puede cerrar la unidad: " + pendientes +
                    (pendientes == 1 ? " alumno tiene" : " alumnos tienen") + " calificaciones pendientes.");
        }
        guardarEstado(grupoId, unidadId, "CERRADA");
    }

    @Transactional
    public void abrirUnidad(Integer grupoId, Integer unidadId) {
        grupoRepository.findById(grupoId).ifPresent(g -> {
            if (g.isCerrado()) {
                throw new IllegalStateException("No se puede reabrir la unidad: la evaluación del grupo ya fue terminada.");
            }
        });
        guardarEstado(grupoId, unidadId, "ABIERTA");
    }

    @Transactional(readOnly = true)
    public Map<Integer, String> obtenerEstadosPorGrupo(Integer grupoId) {
        return estadoUnidadRepository.findByGrupoId(grupoId).stream()
                .collect(Collectors.toMap(e -> e.getUnidad().getId(), EstadoUnidad::getEstado));
    }

    @Transactional(readOnly = true)
    public boolean todasUnidadesCerradas(Integer grupoId, Integer totalUnidades) {
        if (totalUnidades == 0) return true;
        long cerradas = estadoUnidadRepository.findByGrupoId(grupoId).stream()
                .filter(EstadoUnidad::isCerrada).count();
        return cerradas >= totalUnidades;
    }
}
