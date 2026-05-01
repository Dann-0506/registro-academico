package com.sira.service;

import com.sira.dto.CalificacionFinalDto;
import com.sira.dto.KpiResponse;
import com.sira.dto.RendimientoResponse;
import com.sira.model.Grupo;
import com.sira.repository.AlumnoRepository;
import com.sira.repository.GrupoRepository;
import com.sira.repository.MateriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AnalisisService {

    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private MateriaRepository materiaRepository;
    @Autowired private GrupoRepository grupoRepository;
    @Autowired private ReporteService reporteService;
    @Autowired private CalificacionService calificacionService;

    @Transactional(readOnly = true)
    public KpiResponse obtenerKpis() {
        int totalAlumnos = (int) alumnoRepository.count();
        int totalMaterias = (int) materiaRepository.count();
        long gruposActivos = grupoRepository.findAll().stream().filter(Grupo::isActivo).count();
        return new KpiResponse(totalAlumnos, totalMaterias, gruposActivos);
    }

    @Transactional(readOnly = true)
    public List<RendimientoResponse> obtenerRendimientoPorSemestre() {
        List<Grupo> grupos = grupoRepository.findAllWithDetails();
        Map<String, int[]> conteo = new TreeMap<>();

        for (Grupo grupo : grupos) {
            String semestre = grupo.getSemestre() != null && !grupo.getSemestre().isBlank()
                    ? grupo.getSemestre() : "Sin asignar";
            conteo.putIfAbsent(semestre, new int[]{0, 0});

            List<CalificacionFinalDto> calificaciones = reporteService.generarReporteFinalGrupo(
                    grupo.getId(), grupo.getCalificacionMaxima());

            for (CalificacionFinalDto cf : calificaciones) {
                String estado = calificacionService.determinarEstado(
                        cf.getCalificacionFinal(), grupo.getCalificacionMinimaAprobatoria());
                if ("APROBADO".equals(estado)) conteo.get(semestre)[0]++;
                else if ("REPROBADO".equals(estado)) conteo.get(semestre)[1]++;
            }
        }

        List<RendimientoResponse> resultado = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : conteo.entrySet()) {
            resultado.add(new RendimientoResponse(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }
        return resultado;
    }
}
