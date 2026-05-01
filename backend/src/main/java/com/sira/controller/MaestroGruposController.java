package com.sira.controller;

import com.sira.dto.CalificacionFinalDto;
import com.sira.dto.GrupoResponse;
import com.sira.model.Grupo;
import com.sira.model.Maestro;
import com.sira.model.Usuario;
import com.sira.service.GrupoService;
import com.sira.service.MaestroService;
import com.sira.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maestro")
@PreAuthorize("hasRole('MAESTRO')")
public class MaestroGruposController {

    @Autowired private GrupoService grupoService;
    @Autowired private MaestroService maestroService;
    @Autowired private ReporteService reporteService;

    @GetMapping("/grupos")
    public List<GrupoResponse> misGrupos(@AuthenticationPrincipal Usuario usuario) {
        Maestro maestro = maestroService.buscarPorUsuarioId(usuario.getId());
        return grupoService.buscarPorMaestro(maestro.getId()).stream().map(GrupoResponse::from).toList();
    }

    @GetMapping("/grupos/{id}")
    public GrupoResponse obtenerGrupo(@PathVariable Integer id, @AuthenticationPrincipal Usuario usuario) {
        Grupo grupo = grupoService.buscarPorId(id);
        verificarPropietario(grupo, usuario);
        return GrupoResponse.from(grupo);
    }

    @GetMapping("/grupos/{id}/reporte")
    public List<CalificacionFinalDto> reporte(@PathVariable Integer id, @AuthenticationPrincipal Usuario usuario) {
        Grupo grupo = grupoService.buscarPorId(id);
        verificarPropietario(grupo, usuario);
        return reporteService.generarReporteFinalGrupo(id, grupo.getCalificacionMaxima());
    }

    @PostMapping("/grupos/{id}/cerrar")
    public ResponseEntity<Void> cerrar(@PathVariable Integer id, @AuthenticationPrincipal Usuario usuario) {
        verificarPropietario(grupoService.buscarPorId(id), usuario);
        grupoService.cerrarCurso(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/grupos/{id}/reabrir")
    public ResponseEntity<Void> reabrir(@PathVariable Integer id, @AuthenticationPrincipal Usuario usuario) {
        verificarPropietario(grupoService.buscarPorId(id), usuario);
        grupoService.reabrirCurso(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/grupos/{id}/cerrar-definitivo")
    public ResponseEntity<Void> cerrarDefinitivamente(@PathVariable Integer id, @AuthenticationPrincipal Usuario usuario) {
        verificarPropietario(grupoService.buscarPorId(id), usuario);
        grupoService.cerrarDefinitivamente(id);
        return ResponseEntity.noContent().build();
    }

    private void verificarPropietario(Grupo grupo, Usuario usuario) {
        Maestro maestro = maestroService.buscarPorUsuarioId(usuario.getId());
        if (!grupo.getMaestro().getId().equals(maestro.getId())) {
            throw new IllegalStateException("No tienes permiso para acceder a este grupo.");
        }
    }
}
