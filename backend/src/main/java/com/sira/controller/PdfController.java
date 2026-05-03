package com.sira.controller;

import com.sira.dto.CalificacionFinalDto;
import com.sira.model.Grupo;
import com.sira.model.Unidad;
import com.sira.model.Usuario;
import com.sira.model.Maestro;
import com.sira.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class PdfController {

    @Autowired private GrupoService grupoService;
    @Autowired private MaestroService maestroService;
    @Autowired private AlumnoService alumnoService;
    @Autowired private UnidadService unidadService;
    @Autowired private InscripcionService inscripcionService;
    @Autowired private ReporteService reporteService;
    @Autowired private ExportadorPdfService exportadorPdfService;

    @GetMapping("/maestro/grupos/{grupoId}/reporte/pdf")
    @PreAuthorize("hasRole('MAESTRO') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> actaGrupo(@PathVariable Integer grupoId,
                                             @AuthenticationPrincipal Usuario usuario) {
        Grupo grupo = grupoService.buscarPorId(grupoId);

        if ("MAESTRO".equals(usuario.getRol())) {
            Maestro maestro = maestroService.buscarPorUsuarioId(usuario.getId());
            if (!grupo.getMaestro().getId().equals(maestro.getId())) {
                throw new IllegalStateException("No tienes permiso para acceder a este grupo.");
            }
        }

        List<Unidad> unidades = unidadService.listarPorGrupo(grupoId);
        List<CalificacionFinalDto> reporte = reporteService.generarReporteFinalGrupo(
                grupoId, grupo.getCalificacionMaxima());

        byte[] pdf = exportadorPdfService.generarActa(grupo, unidades, reporte);
        return pdfResponse(pdf, "acta_grupo_" + grupoId + ".pdf");
    }


    @GetMapping("/alumno/cursos/{grupoId}/boleta/pdf")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<byte[]> boletaAlumno(@PathVariable Integer grupoId,
                                                @AuthenticationPrincipal Usuario usuario) {
        var alumno = alumnoService.buscarPorUsuarioId(usuario.getId());
        var inscripcion = inscripcionService.listarPorAlumno(alumno.getId()).stream()
                .filter(i -> i.getGrupo().getId().equals(grupoId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No estás inscrito en este grupo."));
        return generarBoleta(inscripcion.getId());
    }

    private ResponseEntity<byte[]> generarBoleta(Integer inscripcionId) {
        var inscripcion = inscripcionService.buscarPorId(inscripcionId);
        Grupo grupo = grupoService.buscarPorId(inscripcion.getGrupo().getId());
        List<CalificacionFinalDto> reporte = reporteService.generarReporteFinalGrupo(
                grupo.getId(), grupo.getCalificacionMaxima());

        CalificacionFinalDto cf = reporte.stream()
                .filter(c -> c.getAlumnoId().equals(inscripcion.getAlumno().getId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No se encontraron calificaciones para esta inscripción."));

        byte[] pdf = exportadorPdfService.generarBoleta(grupo, cf);
        return pdfResponse(pdf, "boleta_" + inscripcion.getAlumno().getMatricula() + ".pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String nombre) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
