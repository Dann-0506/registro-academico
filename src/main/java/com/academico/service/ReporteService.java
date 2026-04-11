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
    public List<CalificacionFinal> generarReporteFinalGrupo(int grupoId) throws Exception {
        List<CalificacionFinal> reporteGrupo = new ArrayList<>();
        
        // 1. Obtener estructura y alumnos inscritos
        List<Inscripcion> inscripciones = inscripcionService.listarPorGrupo(grupoId);
        List<Unidad> unidades = unidadService.listarPorGrupo(grupoId);

        // 2. Procesar cada alumno usando la lógica centralizada
        for (Inscripcion inscripcion : inscripciones) {
            reporteGrupo.add(procesarCalificacionAlumno(inscripcion, unidades));
        }

        return reporteGrupo;
    }

    private CalificacionFinal procesarCalificacionAlumno(Inscripcion inscripcion, List<Unidad> unidades) throws Exception {
        Alumno alumno = alumnoService.buscarPorId(inscripcion.getAlumnoId());
        List<ResultadoUnidad> resultadosUnidades = new ArrayList<>();

        for (Unidad unidad : unidades) {
            resultadosUnidades.add(procesarResultadoUnidad(inscripcion.getId(), unidad));
        }

        BigDecimal extraMateria = bonusService.obtenerBonusMateria(inscripcion.getId())
                .map(Bonus::getPuntos)
                .orElse(BigDecimal.ZERO);

        // Orquestación final con el nuevo CalificacionService dinámico
        return calificacionService.calcularCalificacionFinal(
                inscripcion.getId(), 
                alumno, 
                resultadosUnidades, 
                extraMateria, 
                inscripcion.getCalificacionFinalOverride(), 
                inscripcion.getOverrideJustificacion()
        );
    }

    private ResultadoUnidad procesarResultadoUnidad(int inscripcionId, Unidad unidad) throws Exception {
        List<Resultado> resultados = resultadoService.buscarPorInscripcionYUnidad(inscripcionId, unidad.getId());
        
        BigDecimal puntosExtra = bonusService.obtenerBonusUnidad(inscripcionId, unidad.getId())
                .map(Bonus::getPuntos)
                .orElse(BigDecimal.ZERO);

        // Llama al método que ahora es sensible a la configuración de la BD
        return calificacionService.calcularResultadoUnidad(inscripcionId, unidad, resultados, puntosExtra);
    }
}