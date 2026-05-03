package com.sira.service;

import com.sira.model.Bonus;
import com.sira.model.Inscripcion;
import com.sira.model.Unidad;
import com.sira.repository.BonusRepository;
import com.sira.repository.InscripcionRepository;
import com.sira.repository.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BonusService {

    @Autowired private BonusRepository bonusRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private UnidadRepository unidadRepository;

    @Transactional(readOnly = true)
    public Optional<Bonus> obtenerBonusUnidad(Integer inscripcionId, Integer unidadId) {
        if (inscripcionId == null || unidadId == null) return Optional.empty();
        return bonusRepository.findByInscripcionIdAndUnidadIdAndTipo(inscripcionId, unidadId, "unidad");
    }

    @Transactional(readOnly = true)
    public Optional<Bonus> obtenerBonusMateria(Integer inscripcionId) {
        if (inscripcionId == null) return Optional.empty();
        return bonusRepository.findByInscripcionIdAndTipoAndUnidadIsNull(inscripcionId, "materia");
    }

    @Transactional(readOnly = true)
    public List<Bonus> obtenerHistorial(Integer inscripcionId) {
        return bonusRepository.findByInscripcionIdOrderByOtorgadoEnAsc(inscripcionId);
    }

    @Transactional
    public Bonus guardar(Integer inscripcionId, Integer unidadId, String tipo, BigDecimal puntos, String justificacion) {
        if (puntos == null || puntos.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Los puntos extra deben ser mayores a 0.");
        }
        if (justificacion == null || justificacion.isBlank()) {
            throw new IllegalArgumentException("Es obligatorio justificar el motivo de los puntos extra.");
        }
        if (tipo == null || (!tipo.equals("unidad") && !tipo.equals("materia"))) {
            throw new IllegalArgumentException("El tipo debe ser 'unidad' o 'materia'.");
        }

        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new NoSuchElementException("Inscripción no encontrada: " + inscripcionId));

        if (inscripcion.getGrupo().isCerrado()) {
            throw new IllegalStateException("No se pueden otorgar puntos extra: la evaluación del grupo ya fue terminada.");
        }

        Unidad unidad = null;
        if ("unidad".equals(tipo)) {
            if (unidadId == null) throw new IllegalArgumentException("Se requiere unidadId para bonus de tipo 'unidad'.");
            unidad = unidadRepository.findById(unidadId)
                    .orElseThrow(() -> new NoSuchElementException("Unidad no encontrada: " + unidadId));
        }

        return bonusRepository.save(new Bonus(inscripcion, unidad, tipo, puntos, justificacion.trim()));
    }
}
