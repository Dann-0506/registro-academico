package com.academico.service;

import com.academico.model.CalificacionFinal;
import com.academico.model.Grupo;
import com.academico.service.individuals.AlumnoService;
import com.academico.service.individuals.GrupoService;
import com.academico.service.individuals.MateriaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnalisisService {

    // Instanciamos los servicios individuales solo aquí
    private final AlumnoService alumnoService = new AlumnoService();
    private final GrupoService grupoService = new GrupoService();
    private final MateriaService materiaService = new MateriaService();
    private final ReporteService reporteService = new ReporteService();
    private final CalificacionService calificacionService = new CalificacionService();

    // === DTOs (Data Transfer Objects) usando Records de Java 21 ===
    public record KpiDTO(int totalAlumnos, int totalMaterias, long gruposActivos) {}
    public record RendimientoDTO(String semestre, int aprobados, int reprobados) {}

    // === LÓGICA DE NEGOCIO ===

    public KpiDTO obtenerKpisGenerales() throws Exception {
        int alumnos = alumnoService.listarTodos().size();
        int materias = materiaService.listarTodas().size();
        long grupos = grupoService.listarTodos().stream().filter(Grupo::isActivo).count();
        
        return new KpiDTO(alumnos, materias, grupos);
    }

    public List<RendimientoDTO> obtenerDatosRendimiento() throws Exception {
        List<Grupo> grupos = grupoService.listarTodos();
        
        // Usamos un arreglo de enteros para contar [Aprobados, Reprobados]
        Map<String, int[]> conteoPorSemestre = new TreeMap<>(); 

        for (Grupo grupo : grupos) {
            String semestre = grupo.getSemestre();
            if (semestre == null || semestre.trim().isEmpty()) {
                semestre = "Sin asignar";
            }

            conteoPorSemestre.putIfAbsent(semestre, new int[]{0, 0});

            List<CalificacionFinal> calificaciones = reporteService.generarReporteFinalGrupo(grupo.getId());

            for (CalificacionFinal cf : calificaciones) {
                try {
                    String estado = calificacionService.determinarEstado(cf.getCalificacionFinal());
                    if ("APROBADO".equals(estado)) {
                        conteoPorSemestre.get(semestre)[0]++; // Suma Aprobado
                    } else if ("REPROBADO".equals(estado)) {
                        conteoPorSemestre.get(semestre)[1]++; // Suma Reprobado
                    }
                } catch (Exception ignored) {
                    // Ignoramos los estados PENDIENTE o errores
                }
            }
        }

        // Convertimos el mapa a una lista de DTOs limpios
        List<RendimientoDTO> resultado = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : conteoPorSemestre.entrySet()) {
            resultado.add(new RendimientoDTO(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }

        return resultado;
    }
}