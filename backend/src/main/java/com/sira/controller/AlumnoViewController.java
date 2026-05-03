package com.sira.controller;

import com.sira.dto.CalificacionFinalDto;
import com.sira.dto.GrupoResponse;
import com.sira.dto.InscripcionResponse;
import com.sira.model.Alumno;
import com.sira.model.Grupo;
import com.sira.model.Inscripcion;
import com.sira.model.Usuario;
import com.sira.service.AlumnoService;
import com.sira.service.GrupoService;
import com.sira.service.InscripcionService;
import com.sira.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumno")
@PreAuthorize("hasRole('ALUMNO')")
public class AlumnoViewController {

    @Autowired private AlumnoService alumnoService;
    @Autowired private GrupoService grupoService;
    @Autowired private InscripcionService inscripcionService;
    @Autowired private ReporteService reporteService;

    @GetMapping("/mis-cursos")
    public List<GrupoResponse> misCursos(@AuthenticationPrincipal Usuario usuario) {
        Alumno alumno = alumnoService.buscarPorUsuarioId(usuario.getId());
        return grupoService.buscarPorAlumno(alumno.getId()).stream().map(GrupoResponse::from).toList();
    }

    @GetMapping("/mis-inscripciones")
    public List<InscripcionResponse> misInscripciones(@AuthenticationPrincipal Usuario usuario) {
        Alumno alumno = alumnoService.buscarPorUsuarioId(usuario.getId());
        return inscripcionService.listarPorAlumno(alumno.getId()).stream().map(InscripcionResponse::from).toList();
    }

    @GetMapping("/cursos/{grupoId}/calificaciones")
    public CalificacionFinalDto misCalificaciones(@PathVariable Integer grupoId,
                                                   @AuthenticationPrincipal Usuario usuario) {
        Alumno alumno = alumnoService.buscarPorUsuarioId(usuario.getId());
        Inscripcion inscripcion = inscripcionService.listarPorAlumno(alumno.getId()).stream()
                .filter(i -> i.getGrupo().getId().equals(grupoId))
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("No estás inscrito en este grupo."));

        Grupo grupo = grupoService.buscarPorId(grupoId);
        List<CalificacionFinalDto> reporte = reporteService.generarReporteFinalGrupo(
                grupoId, grupo.getCalificacionMaxima());

        return reporte.stream()
                .filter(cf -> cf.getAlumnoId().equals(alumno.getId()))
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("No se encontraron calificaciones."));
    }
}
