package com.sira.service;

import com.sira.dto.CalificacionFinalDto;
import com.sira.model.Grupo;
import com.sira.model.Inscripcion;
import com.sira.model.Maestro;
import com.sira.model.Materia;
import com.sira.repository.GrupoRepository;
import com.sira.repository.InscripcionRepository;
import com.sira.repository.MaestroRepository;
import com.sira.repository.MateriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class GrupoService {

    @Autowired private GrupoRepository grupoRepository;
    @Autowired private MateriaRepository materiaRepository;
    @Autowired private MaestroRepository maestroRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private ConfiguracionService configuracionService;
    @Lazy @Autowired private ReporteService reporteService;

    @Transactional(readOnly = true)
    public List<Grupo> listarTodos() {
        return grupoRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Grupo buscarPorId(Integer id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Grupo> buscarPorMaestro(Integer maestroId) {
        return grupoRepository.findByMaestroIdAbiertos(maestroId);
    }

    @Transactional(readOnly = true)
    public List<Grupo> buscarPorAlumno(Integer alumnoId) {
        return grupoRepository.findByAlumnoId(alumnoId);
    }

    @Transactional(readOnly = true)
    public Grupo buscarPorClaveYSemestre(String clave, String semestre) {
        return grupoRepository.findByClaveAndSemestre(clave, semestre)
                .orElseThrow(() -> new NoSuchElementException(
                        "Grupo '" + clave + "' no encontrado para el semestre '" + semestre + "'"));
    }

    @Transactional
    public Grupo crear(Integer materiaId, Integer maestroId, String clave, String semestre,
                       BigDecimal calMinima, BigDecimal calMaxima) {
        validarCampos(materiaId, maestroId, clave, semestre);

        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada con id: " + materiaId));
        Maestro maestro = maestroRepository.findById(maestroId)
                .orElseThrow(() -> new NoSuchElementException("Maestro no encontrado con id: " + maestroId));

        if (grupoRepository.existsByClaveAndMateriaIdAndSemestre(clave, materiaId, semestre)) {
            throw new IllegalStateException("Ya existe un grupo con esa clave para la misma materia y semestre.");
        }

        Grupo grupo = new Grupo(materia, maestro, clave.trim().toUpperCase(), semestre.trim());
        grupo.setCalificacionMinimaAprobatoria(calMinima != null ? calMinima : configuracionService.obtenerCalificacionMinima());
        grupo.setCalificacionMaxima(calMaxima != null ? calMaxima : configuracionService.obtenerCalificacionMaxima());
        return grupoRepository.save(grupo);
    }

    @Transactional
    public Grupo actualizar(Integer id, Integer materiaId, Integer maestroId, String clave, String semestre) {
        Grupo grupo = buscarPorId(id);
        validarCampos(materiaId, maestroId, clave, semestre);

        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada con id: " + materiaId));
        Maestro maestro = maestroRepository.findById(maestroId)
                .orElseThrow(() -> new NoSuchElementException("Maestro no encontrado con id: " + maestroId));

        grupo.setMateria(materia);
        grupo.setMaestro(maestro);
        grupo.setClave(clave.trim().toUpperCase());
        grupo.setSemestre(semestre.trim());
        return grupoRepository.save(grupo);
    }

    @Transactional
    public void cambiarEstado(Integer id, boolean activo) {
        Grupo grupo = buscarPorId(id);
        grupo.setActivo(activo);
        grupoRepository.save(grupo);
    }

    @Transactional
    public void cerrarCurso(Integer id) {
        Grupo grupo = buscarPorId(id);
        if (grupo.isCerrado()) {
            throw new IllegalStateException("El curso ya se encuentra cerrado.");
        }
        // Congelar snapshot de calificaciones antes de cerrar
        congelarCalificacionesFinales(grupo);
        grupo.setEstadoEvaluacion("CERRADO");
        grupoRepository.save(grupo);
    }

    private void congelarCalificacionesFinales(Grupo grupo) {
        List<CalificacionFinalDto> reporte = reporteService.generarReporteFinalGrupo(
                grupo.getId(), grupo.getCalificacionMaxima());
        for (CalificacionFinalDto cf : reporte) {
            if (cf.getCalificacionFinal() != null) {
                String estado = cf.getCalificacionFinal()
                        .compareTo(grupo.getCalificacionMinimaAprobatoria()) >= 0 ? "APROBADO" : "REPROBADO";
                inscripcionRepository.guardarResultadoHistorico(cf.getInscripcionId(), cf.getCalificacionFinal(), estado);
            }
        }
    }

    @Transactional
    public void reabrirCurso(Integer id) {
        Grupo grupo = buscarPorId(id);
        grupo.setEstadoEvaluacion("ABIERTO");
        grupo.setActivo(true);
        grupoRepository.save(grupo);

        List<Inscripcion> inscripciones = inscripcionRepository.findByGrupoIdWithAlumno(id);
        for (Inscripcion inscripcion : inscripciones) {
            inscripcionRepository.guardarResultadoHistorico(inscripcion.getId(), null, "PENDIENTE");
        }
    }

    @Transactional
    public void eliminar(Integer id) {
        Grupo grupo = buscarPorId(id);
        if (grupo.isCerrado()) {
            throw new IllegalStateException("No se puede eliminar un grupo con acta cerrada.");
        }
        if (inscripcionRepository.existsByGrupoId(id)) {
            throw new IllegalStateException("No se puede eliminar: el grupo tiene alumnos inscritos.");
        }
        grupoRepository.delete(grupo);
    }

    private void validarCampos(Integer materiaId, Integer maestroId, String clave, String semestre) {
        if (materiaId == null || materiaId <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una materia válida.");
        }
        if (maestroId == null || maestroId <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un docente válido.");
        }
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("La clave del grupo es obligatoria.");
        }
        if (semestre == null || semestre.isBlank()) {
            throw new IllegalArgumentException("El semestre es obligatorio.");
        }
    }
}
