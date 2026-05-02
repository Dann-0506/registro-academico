package com.sira.service;

import com.sira.dto.CalificacionFinalDto;
import com.sira.dto.ResultadoDto;
import com.sira.dto.ResultadoUnidadDto;
import com.sira.model.Bonus;
import com.sira.model.Inscripcion;
import com.sira.model.Resultado;
import com.sira.model.Unidad;
import com.sira.repository.InscripcionRepository;
import com.sira.repository.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteService {

    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private UnidadRepository unidadRepository;
    @Autowired private ResultadoService resultadoService;
    @Autowired private BonusService bonusService;
    @Autowired private CalificacionService calificacionService;

    @Transactional(readOnly = true)
    public List<CalificacionFinalDto> generarReporteFinalGrupo(Integer grupoId, BigDecimal limiteMaximo) {
        List<Inscripcion> inscripciones = inscripcionRepository.findByGrupoIdWithAlumno(grupoId);
        List<Unidad> unidades = unidadRepository.findByMateriaIdOrderByNumeroAsc(
                inscripciones.isEmpty() ? 0 : inscripciones.get(0).getGrupo().getMateria().getId()
        );

        List<CalificacionFinalDto> reporte = new ArrayList<>();
        for (Inscripcion inscripcion : inscripciones) {
            reporte.add(procesarCalificacionAlumno(inscripcion, unidades, limiteMaximo));
        }
        return reporte;
    }

    private CalificacionFinalDto procesarCalificacionAlumno(
            Inscripcion inscripcion, List<Unidad> unidades, BigDecimal limiteMaximo) {

        // Si el acta ya fue cerrada, devolver la fotografía histórica
        if (inscripcion.getCalificacionFinalCalculada() != null) {
            return construirHistorica(inscripcion, unidades, limiteMaximo);
        }

        // Grupo abierto: calcular on-demand
        String nombre = inscripcion.getAlumno().getUsuario().getNombre();
        String numControl = inscripcion.getAlumno().getMatricula();

        List<ResultadoUnidadDto> resultadosUnidades = new ArrayList<>();
        for (Unidad unidad : unidades) {
            resultadosUnidades.add(procesarResultadoUnidad(inscripcion.getId(), unidad, limiteMaximo));
        }

        BigDecimal bonusMateria = bonusService.obtenerBonusMateria(inscripcion.getId())
                .map(Bonus::getPuntos)
                .orElse(BigDecimal.ZERO);

        return calificacionService.calcularCalificacionFinal(
                inscripcion.getId(),
                inscripcion.getAlumno().getId(),
                nombre,
                numControl,
                resultadosUnidades,
                bonusMateria,
                inscripcion.getCalificacionFinalOverride(),
                inscripcion.getOverrideJustificacion(),
                limiteMaximo
        );
    }

    private CalificacionFinalDto construirHistorica(
            Inscripcion inscripcion, List<Unidad> unidades, BigDecimal limiteMaximo) {

        List<ResultadoUnidadDto> resultadosUnidades = new ArrayList<>();
        for (Unidad unidad : unidades) {
            resultadosUnidades.add(procesarResultadoUnidad(inscripcion.getId(), unidad, limiteMaximo));
        }

        BigDecimal finalDefinitiva = inscripcion.getCalificacionFinalOverride() != null
                ? inscripcion.getCalificacionFinalOverride()
                : inscripcion.getCalificacionFinalCalculada();

        CalificacionFinalDto cf = new CalificacionFinalDto();
        cf.setInscripcionId(inscripcion.getId());
        cf.setAlumnoId(inscripcion.getAlumno().getId());
        cf.setAlumnoNombre(inscripcion.getAlumno().getUsuario().getNombre());
        cf.setAlumnoNumControl(inscripcion.getAlumno().getMatricula());
        cf.setUnidades(resultadosUnidades);
        cf.setCalificacionCalculada(inscripcion.getCalificacionFinalCalculada());
        cf.setCalificacionFinal(finalDefinitiva);
        cf.setEsOverride(inscripcion.getCalificacionFinalOverride() != null);
        cf.setOverrideJustificacion(inscripcion.getOverrideJustificacion());
        return cf;
    }

    private ResultadoUnidadDto procesarResultadoUnidad(Integer inscripcionId, Unidad unidad, BigDecimal limiteMaximo) {
        List<Resultado> resultados = resultadoService.buscarPorInscripcionYUnidad(inscripcionId, unidad.getId());
        BigDecimal bonusUnidad = bonusService.obtenerBonusUnidad(inscripcionId, unidad.getId())
                .map(Bonus::getPuntos)
                .orElse(BigDecimal.ZERO);

        ResultadoUnidadDto ru = calificacionService.calcularResultadoUnidad(
                inscripcionId, unidad, resultados, bonusUnidad, limiteMaximo);
        ru.setDesglose(resultados.stream().map(ResultadoDto::from).toList());
        return ru;
    }
}
