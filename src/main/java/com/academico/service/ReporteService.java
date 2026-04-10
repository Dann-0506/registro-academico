package com.academico.service;

import com.academico.model.*;
import com.academico.service.individuals.AlumnoService;
import com.academico.service.individuals.BonusService;
import com.academico.service.individuals.InscripcionService;
import com.academico.service.individuals.ResultadoService;
import com.academico.service.individuals.UnidadService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio Orquestador de Reportes.
 * Reúne datos de múltiples servicios para generar cálculos académicos complejos.
 */
public class ReporteService {

    // Dependencias de Servicios (Arquitectura de Capas)
    private final AlumnoService alumnoService;
    private final InscripcionService inscripcionService;
    private final UnidadService unidadService;
    private final BonusService bonusService;
    private final CalificacionService calificacionService;
    private final ResultadoService resultadoService;

    public ReporteService() {
        this.alumnoService = new AlumnoService();
        this.inscripcionService = new InscripcionService();
        this.unidadService = new UnidadService();
        this.bonusService = new BonusService();
        this.calificacionService = new CalificacionService();
        this.resultadoService = new ResultadoService();
    }

    public ReporteService(AlumnoService alumnoService, InscripcionService inscripcionService, UnidadService unidadService, 
                          BonusService bonusService, CalificacionService calificacionService, ResultadoService resultadoService) {
        this.alumnoService = alumnoService;
        this.inscripcionService = inscripcionService;
        this.unidadService = unidadService;
        this.bonusService = bonusService;
        this.calificacionService = calificacionService;
        this.resultadoService = resultadoService;
    }

    /**
     * Genera el desglose completo de calificaciones de un grupo.
     * @param grupoId ID del grupo a consultar.
     * @return Lista de CalificacionFinal con el detalle por alumno y unidad.
     * @throws Exception Si ocurre un error en cualquiera de los servicios de consulta.
     */
    public List<CalificacionFinal> generarReporteFinalGrupo(int grupoId) throws Exception {
        List<CalificacionFinal> reporteGrupo = new ArrayList<>();
        
        // 1. Obtener la estructura del grupo (Inscritos y Unidades)
        List<Inscripcion> inscripciones = inscripcionService.listarPorGrupo(grupoId);
        List<Unidad> unidades = unidadService.listarPorGrupo(grupoId);

        for (Inscripcion inscripcion : inscripciones) {
            
            // 2. Obtener datos del alumno mediante su servicio
            Alumno alumno = alumnoService.buscarPorId(inscripcion.getAlumnoId());
            
            List<ResultadoUnidad> resultadosUnidades = new ArrayList<>();

            // 3. Procesar cada unidad académica
            for (Unidad unidad : unidades) {
                // Consultar calificaciones mediante el servicio de resultados
                List<Resultado> resultados = resultadoService.buscarPorInscripcionYUnidad(
                        inscripcion.getId(), unidad.getId());
                
                // Consultar puntos extra mediante el servicio de bonus
                BigDecimal puntosExtra = bonusService.obtenerBonusUnidad(inscripcion.getId(), unidad.getId())
                        .map(Bonus::getPuntos)
                        .orElse(BigDecimal.ZERO);

                // Calcular el desempeño de la unidad
                ResultadoUnidad ru = calificacionService.calcularResultadoUnidad(
                        inscripcion.getId(), unidad, resultados, puntosExtra);
                
                resultadosUnidades.add(ru);
            }

            // 4. Consultar bonus global de la materia
            BigDecimal extraMateria = bonusService.obtenerBonusMateria(inscripcion.getId())
                    .map(Bonus::getPuntos)
                    .orElse(BigDecimal.ZERO);

            // 5. Generar el cálculo final (delegado al servicio de lógica pura)
            CalificacionFinal calificacionFinal = calificacionService.calcularCalificacionFinal(
                    inscripcion.getId(), 
                    alumno, 
                    resultadosUnidades, 
                    extraMateria, 
                    inscripcion.getCalificacionFinalOverride(), 
                    inscripcion.getOverrideJustificacion()
            );

            reporteGrupo.add(calificacionFinal);
        }

        return reporteGrupo;
    }
}