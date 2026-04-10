package com.academico.service.individuals;

import com.academico.model.*;
import com.academico.service.CalificacionService;

import java.util.ArrayList;
import java.util.List;

public class ReporteService {
    // Dependencias de Servicios en lugar de DAOs
    private final AlumnoService alumnoService = new AlumnoService();
    private final UnidadService unidadService = new UnidadService();
    private final BonusService bonusService = new BonusService();
    private final CalificacionService calificacionService = new CalificacionService();
    
    // El resultadoDAO se mantiene o se crea un ResultadoService para lectura
    private final com.academico.dao.ResultadoDAO resultadoDAO = new com.academico.dao.ResultadoDAO();

    public List<CalificacionFinal> generarReporteFinalGrupo(int grupoId) throws Exception {
        List<CalificacionFinal> reporteGrupo = new ArrayList<>();
        
        // Usamos servicios para obtener la estructura
        List<Inscripcion> inscripciones = new com.academico.dao.InscripcionDAO().findByGrupo(grupoId);
        List<Unidad> unidades = unidadService.listarPorGrupo(grupoId);

        for (Inscripcion inscripcion : inscripciones) {
            // Buscamos al alumno a través de su servicio
            Alumno alumno = alumnoService.listarTodos().stream()
                    .filter(a -> a.getId() == inscripcion.getAlumnoId())
                    .findFirst().orElse(new Alumno());
            
            List<ResultadoUnidad> resultadosUnidades = new ArrayList<>();

            for (Unidad unidad : unidades) {
                List<Resultado> resultados = resultadoDAO.findByInscripcionYUnidad(inscripcion.getId(), unidad.getId());
                
                // Bonus obtenido mediante el servicio
                java.math.BigDecimal puntosExtra = bonusService.obtenerBonusUnidad(inscripcion.getId(), unidad.getId())
                        .map(Bonus::getPuntos).orElse(null);

                ResultadoUnidad ru = calificacionService.calcularResultadoUnidad(
                        inscripcion.getId(), unidad, resultados, puntosExtra);
                
                resultadosUnidades.add(ru);
            }

            java.math.BigDecimal extraMateria = bonusService.obtenerBonusMateria(inscripcion.getId())
                    .map(Bonus::getPuntos).orElse(null);

            // Cálculo final delegado al servicio matemático
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