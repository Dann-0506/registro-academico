package com.sira.service;

import com.sira.dto.ResultadoItemRequest;
import com.sira.model.Resultado;
import com.sira.repository.ActividadGrupoRepository;
import com.sira.repository.GrupoRepository;
import com.sira.repository.ResultadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ResultadoService {

    private static final BigDecimal PONDERACION_COMPLETA = new BigDecimal("100.00");

    @Autowired private ResultadoRepository resultadoRepository;
    @Autowired private EstadoUnidadService estadoUnidadService;
    @Autowired private ActividadGrupoRepository actividadGrupoRepository;
    @Autowired private GrupoRepository grupoRepository;

    @Transactional(readOnly = true)
    public List<Resultado> buscarPorInscripcionYUnidad(Integer inscripcionId, Integer unidadId) {
        if (inscripcionId == null || unidadId == null) return List.of();
        return resultadoRepository.findByInscripcionIdAndUnidadId(inscripcionId, unidadId);
    }

    @Transactional(readOnly = true)
    public List<Resultado> buscarPorInscripcion(Integer inscripcionId) {
        return resultadoRepository.findByInscripcionId(inscripcionId);
    }

    @Transactional
    public void guardarLote(Integer grupoId, Integer unidadId, List<ResultadoItemRequest> items) {
        if (items == null || items.isEmpty()) return;

        BigDecimal maxima = obtenerMaximaDelGrupo(grupoId);

        BigDecimal suma = actividadGrupoRepository.sumPonderacionByGrupoIdAndUnidadId(grupoId, unidadId);
        if (suma == null || suma.compareTo(PONDERACION_COMPLETA) != 0) {
            throw new IllegalStateException(
                    "La ponderación de las actividades no suma 100%. No se pueden registrar calificaciones.");
        }

        estadoUnidadService.validarUnidadAbierta(grupoId, unidadId);

        for (ResultadoItemRequest item : items) {
            if (item.calificacion() != null &&
                    (item.calificacion().compareTo(BigDecimal.ZERO) < 0 ||
                     item.calificacion().compareTo(maxima) > 0)) {
                throw new IllegalArgumentException(
                        "Todas las calificaciones deben estar entre 0 y " + maxima.stripTrailingZeros().toPlainString() + ".");
            }
            resultadoRepository.upsert(item.inscripcionId(), item.actividadGrupoId(), item.calificacion());
        }
    }

    private BigDecimal obtenerMaximaDelGrupo(Integer grupoId) {
        return grupoRepository.findById(grupoId)
                .map(g -> g.getCalificacionMaxima() != null ? g.getCalificacionMaxima() : new BigDecimal("100"))
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado con id: " + grupoId));
    }
}
