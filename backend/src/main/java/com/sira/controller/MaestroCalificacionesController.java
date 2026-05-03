package com.sira.controller;

import com.sira.dto.OverrideRequest;
import com.sira.dto.ResultadoDto;
import com.sira.dto.ResultadoLoteRequest;
import com.sira.model.Grupo;
import com.sira.model.Maestro;
import com.sira.model.Usuario;
import com.sira.service.EstadoUnidadService;
import com.sira.service.GrupoService;
import com.sira.service.InscripcionService;
import com.sira.service.MaestroService;
import com.sira.service.ResultadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maestro")
@PreAuthorize("hasRole('MAESTRO')")
public class MaestroCalificacionesController {

    @Autowired private ResultadoService resultadoService;
    @Autowired private EstadoUnidadService estadoUnidadService;
    @Autowired private GrupoService grupoService;
    @Autowired private MaestroService maestroService;
    @Autowired private InscripcionService inscripcionService;

    @GetMapping("/inscripciones/{inscripcionId}/resultados")
    public List<ResultadoDto> resultadosPorInscripcion(@PathVariable Integer inscripcionId,
                                                        @AuthenticationPrincipal Usuario usuario) {
        verificarPropietarioPorInscripcion(inscripcionId, usuario);
        return resultadoService.buscarPorInscripcion(inscripcionId)
                .stream().map(ResultadoDto::from).toList();
    }

    @PostMapping("/grupos/{grupoId}/calificaciones/lote")
    public ResponseEntity<Void> guardarLote(@PathVariable Integer grupoId,
                                            @RequestBody ResultadoLoteRequest request,
                                            @AuthenticationPrincipal Usuario usuario) {
        verificarPropietario(grupoId, usuario);
        resultadoService.guardarLote(grupoId, request.unidadId(), request.resultados());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/inscripciones/{inscripcionId}/override")
    public ResponseEntity<Void> aplicarOverride(@PathVariable Integer inscripcionId,
                                                 @RequestBody OverrideRequest request,
                                                 @AuthenticationPrincipal Usuario usuario) {
        verificarPropietarioPorInscripcion(inscripcionId, usuario);
        inscripcionService.aplicarOverride(inscripcionId, request.calificacion(), request.justificacion());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grupos/{grupoId}/unidades/{unidadId}/estado")
    public ResponseEntity<Map<String, String>> estadoUnidad(@PathVariable Integer grupoId,
                                                             @PathVariable Integer unidadId,
                                                             @AuthenticationPrincipal Usuario usuario) {
        verificarPropietario(grupoId, usuario);
        String estado = estadoUnidadService.obtenerEstado(grupoId, unidadId)
                .map(e -> e.getEstado())
                .orElse("ABIERTA");
        return ResponseEntity.ok(Map.of("estado", estado));
    }

    @PostMapping("/grupos/{grupoId}/unidades/{unidadId}/cerrar")
    public ResponseEntity<Void> cerrarUnidad(@PathVariable Integer grupoId,
                                              @PathVariable Integer unidadId,
                                              @AuthenticationPrincipal Usuario usuario) {
        verificarPropietario(grupoId, usuario);
        estadoUnidadService.cerrarUnidad(grupoId, unidadId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/grupos/{grupoId}/unidades/{unidadId}/abrir")
    public ResponseEntity<Void> abrirUnidad(@PathVariable Integer grupoId,
                                             @PathVariable Integer unidadId,
                                             @AuthenticationPrincipal Usuario usuario) {
        verificarPropietario(grupoId, usuario);
        estadoUnidadService.abrirUnidad(grupoId, unidadId);
        return ResponseEntity.noContent().build();
    }

    private void verificarPropietarioPorInscripcion(Integer inscripcionId, Usuario usuario) {
        verificarPropietario(inscripcionService.buscarPorId(inscripcionId).getGrupo().getId(), usuario);
    }

    private void verificarPropietario(Integer grupoId, Usuario usuario) {
        Grupo grupo = grupoService.buscarPorId(grupoId);
        Maestro maestro = maestroService.buscarPorUsuarioId(usuario.getId());
        if (!grupo.getMaestro().getId().equals(maestro.getId())) {
            throw new IllegalStateException("No tienes permiso para acceder a este grupo.");
        }
    }
}
