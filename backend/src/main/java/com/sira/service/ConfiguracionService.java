package com.sira.service;

import com.sira.model.Configuracion;
import com.sira.repository.ConfiguracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ConfiguracionService {

    @Autowired private ConfiguracionRepository configuracionRepository;

    @Transactional(readOnly = true)
    public BigDecimal obtenerCalificacionMinima() {
        return configuracionRepository.findById("calificacion_minima_aprobatoria")
                .map(c -> new BigDecimal(c.getValor()))
                .orElse(new BigDecimal("70"));
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerCalificacionMaxima() {
        return configuracionRepository.findById("calificacion_maxima")
                .map(c -> new BigDecimal(c.getValor()))
                .orElse(new BigDecimal("100"));
    }

    @Transactional(readOnly = true)
    public String obtenerSemestreActivo() {
        return configuracionRepository.findById("semestre_activo")
                .map(c -> c.getValor())
                .orElse("");
    }

    @Transactional
    public void actualizar(BigDecimal minima, BigDecimal maxima, String semestreActivo) {
        if (minima == null || maxima == null) {
            throw new IllegalArgumentException("Ambas calificaciones son obligatorias.");
        }
        if (minima.compareTo(BigDecimal.ZERO) < 0 || maxima.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Las calificaciones deben ser números positivos.");
        }
        if (minima.compareTo(maxima) >= 0) {
            throw new IllegalArgumentException("La calificación mínima no puede ser mayor o igual a la máxima.");
        }
        if (semestreActivo == null || semestreActivo.isBlank()) {
            throw new IllegalArgumentException("El semestre activo es obligatorio.");
        }
        configuracionRepository.save(new Configuracion("calificacion_minima_aprobatoria", minima.toPlainString(), "Calificación mínima para aprobar"));
        configuracionRepository.save(new Configuracion("calificacion_maxima", maxima.toPlainString(), "Calificación máxima permitida"));
        configuracionRepository.save(new Configuracion("semestre_activo", semestreActivo.trim(), "Semestre académico activo para el dashboard operativo"));
    }

    // Mantener compatibilidad con servicios que solo necesitan actualizar límites
    @Transactional
    public void actualizarLimites(BigDecimal minima, BigDecimal maxima) {
        actualizar(minima, maxima, obtenerSemestreActivo());
    }
}
