package com.sira.service;

import com.sira.model.Alumno;
import com.sira.model.Grupo;
import com.sira.model.Inscripcion;
import com.sira.repository.AlumnoRepository;
import com.sira.repository.GrupoRepository;
import com.sira.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class InscripcionService {

    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private GrupoRepository grupoRepository;

    @Transactional(readOnly = true)
    public List<Inscripcion> listarTodas() {
        return inscripcionRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public List<Inscripcion> listarPorGrupo(Integer grupoId) {
        return inscripcionRepository.findByGrupoIdWithAlumno(grupoId);
    }

    @Transactional(readOnly = true)
    public List<Inscripcion> listarPorAlumno(Integer alumnoId) {
        return inscripcionRepository.findByAlumnoIdWithGrupo(alumnoId);
    }

    @Transactional(readOnly = true)
    public Inscripcion buscarPorId(Integer id) {
        return inscripcionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NoSuchElementException("Inscripción no encontrada con id: " + id));
    }

    @Transactional
    public Inscripcion inscribir(Integer alumnoId, Integer grupoId, LocalDate fecha) {
        if (alumnoId == null || grupoId == null) {
            throw new IllegalArgumentException("El alumno y el grupo son obligatorios.");
        }
        if (inscripcionRepository.existsByAlumnoIdAndGrupoId(alumnoId, grupoId)) {
            throw new IllegalStateException("El alumno ya se encuentra inscrito en este grupo.");
        }
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new NoSuchElementException("Alumno no encontrado con id: " + alumnoId));
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado con id: " + grupoId));
        if (grupo.isCerrado()) {
            throw new IllegalStateException("No se puede inscribir un alumno: la evaluación del grupo ya fue terminada.");
        }

        return inscripcionRepository.save(new Inscripcion(alumno, grupo, fecha != null ? fecha : LocalDate.now()));
    }

    @Transactional
    public void aplicarOverride(Integer inscripcionId, BigDecimal calificacionManual, String justificacion) {
        if (calificacionManual != null) {
            if (calificacionManual.compareTo(BigDecimal.ZERO) < 0 || calificacionManual.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("La calificación manual debe estar entre 0 y 100.");
            }
            if (justificacion == null || justificacion.isBlank()) {
                throw new IllegalArgumentException("Se requiere una justificación al aplicar calificación manual.");
            }
        }
        if (!inscripcionRepository.existsById(inscripcionId)) {
            throw new NoSuchElementException("Inscripción no encontrada con id: " + inscripcionId);
        }
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId).orElseThrow();
        if (inscripcion.getGrupo().isCerrado()) {
            throw new IllegalStateException("No se puede aplicar un override: la evaluación del grupo ya fue terminada.");
        }
        BigDecimal calMinima = inscripcion.getGrupo().getCalificacionMinimaAprobatoria();
        String estado = calificacionManual != null
                ? (calificacionManual.compareTo(calMinima) >= 0 ? "APROBADO" : "REPROBADO")
                : "PENDIENTE";
        inscripcionRepository.actualizarOverride(inscripcionId, calificacionManual, justificacion, estado);
    }

    @Transactional
    public void eliminar(Integer id) {
        Inscripcion inscripcion = inscripcionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NoSuchElementException("Inscripción no encontrada con id: " + id));
        if ("CERRADO".equals(inscripcion.getGrupo().getEstadoEvaluacion())) {
            throw new IllegalStateException("No se puede eliminar una inscripción: la evaluación del grupo ya fue terminada.");
        }
        inscripcionRepository.deleteById(id);
    }
}
