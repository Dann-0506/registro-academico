package com.academico.service;

import com.academico.model.*;
import com.academico.service.individuals.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReporteService {

    private final AlumnoService alumnoService = new AlumnoService();
    private final InscripcionService inscripcionService = new InscripcionService();
    private final UnidadService unidadService = new UnidadService();
    private final BonusService bonusService = new BonusService();
    private final CalificacionService calificacionService = new CalificacionService();
    private final ResultadoService resultadoService = new ResultadoService();

    /**
     * Genera el reporte completo de calificaciones para un grupo.
     * Este método centraliza la lógica que antes repetías en los bucles de los controladores.
     */
    public List<CalificacionFinal> generarReporteFinalGrupo(int grupoId, BigDecimal limiteMaximoGrupo) throws Exception {
        List<CalificacionFinal> reporteGrupo = new ArrayList<>();
        
        // 1. Obtener estructura y alumnos inscritos
        List<Inscripcion> inscripciones = inscripcionService.listarPorGrupo(grupoId);
        List<Unidad> unidades = unidadService.listarPorGrupo(grupoId);

        // 2. Procesar cada alumno pasándole la regla (límite) histórica
        for (Inscripcion inscripcion : inscripciones) {
            reporteGrupo.add(procesarCalificacionAlumno(inscripcion, unidades, limiteMaximoGrupo));
        }

        return reporteGrupo;
    }

    private CalificacionFinal procesarCalificacionAlumno(Inscripcion inscripcion, List<Unidad> unidades, BigDecimal limiteMaximo) throws Exception {
        Alumno alumno = alumnoService.buscarPorId(inscripcion.getAlumnoId());
        List<ResultadoUnidad> resultadosUnidades = new ArrayList<>();

        for (Unidad unidad : unidades) {
            resultadosUnidades.add(procesarResultadoUnidad(inscripcion.getId(), unidad, limiteMaximo));
        }

        BigDecimal extraMateria = bonusService.obtenerBonusMateria(inscripcion.getId())
                .map(Bonus::getPuntos)
                .orElse(BigDecimal.ZERO);

        // Orquestación final delegando el límite al nuevo CalificacionService determinista
        return calificacionService.calcularCalificacionFinal(
                inscripcion.getId(), 
                alumno, 
                resultadosUnidades, 
                extraMateria, 
                inscripcion.getCalificacionFinalOverride(), 
                inscripcion.getOverrideJustificacion(),
                limiteMaximo // <--- NUEVO PARÁMETRO INYECTADO
        );
    }

    private ResultadoUnidad procesarResultadoUnidad(int inscripcionId, Unidad unidad, BigDecimal limiteMaximo) throws Exception {
        List<Resultado> resultados = resultadoService.buscarPorInscripcionYUnidad(inscripcionId, unidad.getId());
        
        BigDecimal puntosExtra = bonusService.obtenerBonusUnidad(inscripcionId, unidad.getId())
                .map(Bonus::getPuntos)
                .orElse(BigDecimal.ZERO);

        // Llama al método pasando el límite en lugar de que el servicio lo busque
        return calificacionService.calcularResultadoUnidad(inscripcionId, unidad, resultados, puntosExtra, limiteMaximo); // <--- NUEVO PARÁMETRO INYECTADO
    }
}