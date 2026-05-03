package com.sira.service;

import com.sira.dto.AlumnoAlertaDto;
import com.sira.dto.DashboardResponse;
import com.sira.dto.GrupoAlertaDto;
import com.sira.repository.AlumnoRepository;
import com.sira.repository.GrupoRepository;
import com.sira.repository.InscripcionRepository;
import com.sira.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    @Autowired private ConfiguracionService configuracionService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private GrupoRepository grupoRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private AlumnoRepository alumnoRepository;

    @Transactional(readOnly = true)
    public DashboardResponse obtenerDashboard() {
        String semestre = configuracionService.obtenerSemestreActivo();

        long alumnosActivos = usuarioRepository.countByRolAndActivo("alumno", true);
        long maestrosActivos = usuarioRepository.countByRolAndActivo("maestro", true);
        long gruposEnCurso = grupoRepository.countByActivoAndEstadoEvaluacionAndSemestre(true, "ABIERTO", semestre);
        long inscripcionesActivas = inscripcionRepository.countInscripcionesActivasPorSemestre(semestre);

        List<GrupoAlertaDto> sinActividades = grupoRepository.findGruposSinActividades(semestre)
                .stream().map(GrupoAlertaDto::from).toList();

        List<GrupoAlertaDto> pendientesCierre = grupoRepository.findGruposPendientesCierre(semestre)
                .stream().map(GrupoAlertaDto::from).toList();

        List<AlumnoAlertaDto> sinInscripciones = alumnoRepository.findAlumnosSinInscripcionesEnSemestre(semestre)
                .stream().map(AlumnoAlertaDto::from).toList();

        return new DashboardResponse(semestre, alumnosActivos, maestrosActivos, gruposEnCurso,
                inscripcionesActivas, sinActividades, pendientesCierre, sinInscripciones);
    }
}
