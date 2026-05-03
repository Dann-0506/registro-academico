package com.sira.controller;

import com.sira.dto.ActividadCatalogoResponse;
import com.sira.dto.ActividadGrupoRequest;
import com.sira.dto.ActividadGrupoResponse;
import com.sira.model.Grupo;
import com.sira.model.Maestro;
import com.sira.model.Usuario;
import com.sira.service.ActividadCatalogoService;
import com.sira.service.ActividadGrupoService;
import com.sira.service.GrupoService;
import com.sira.service.MaestroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maestro")
@PreAuthorize("hasRole('MAESTRO')")
public class MaestroActividadesController {

    @Autowired private ActividadGrupoService actividadService;
    @Autowired private ActividadCatalogoService catalogoService;
    @Autowired private GrupoService grupoService;
    @Autowired private MaestroService maestroService;

    // Catálogo de actividades disponibles (solo activas)
    @GetMapping("/actividades-catalogo")
    public List<ActividadCatalogoResponse> getCatalogo() {
        return catalogoService.listarActivas().stream().map(ActividadCatalogoResponse::from).toList();
    }

    @GetMapping("/grupos/{grupoId}/actividades")
    public List<ActividadGrupoResponse> listar(@PathVariable Integer grupoId,
                                               @AuthenticationPrincipal Usuario usuario) {
        verificarPropietario(grupoId, usuario);
        return actividadService.listarPorGrupo(grupoId).stream().map(ActividadGrupoResponse::from).toList();
    }

    @PostMapping("/grupos/{grupoId}/actividades")
    public ResponseEntity<ActividadGrupoResponse> crear(@PathVariable Integer grupoId,
                                                        @RequestBody ActividadGrupoRequest request,
                                                        @AuthenticationPrincipal Usuario usuario) {
        verificarPropietario(grupoId, usuario);
        ActividadGrupoResponse response = ActividadGrupoResponse.from(
                actividadService.crear(grupoId, request.unidadId(),
                        request.actividadCatalogoId(), request.etiqueta(), request.ponderacion())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/grupos/{grupoId}/actividades/{id}")
    public ActividadGrupoResponse actualizar(@PathVariable Integer grupoId,
                                             @PathVariable Integer id,
                                             @RequestBody ActividadGrupoRequest request,
                                             @AuthenticationPrincipal Usuario usuario) {
        verificarPropietario(grupoId, usuario);
        return ActividadGrupoResponse.from(
                actividadService.actualizar(id, request.etiqueta(), request.ponderacion()));
    }

    @DeleteMapping("/grupos/{grupoId}/actividades/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer grupoId,
                                         @PathVariable Integer id,
                                         @AuthenticationPrincipal Usuario usuario) {
        verificarPropietario(grupoId, usuario);
        actividadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private void verificarPropietario(Integer grupoId, Usuario usuario) {
        Grupo grupo = grupoService.buscarPorId(grupoId);
        Maestro maestro = maestroService.buscarPorUsuarioId(usuario.getId());
        if (!grupo.getMaestro().getId().equals(maestro.getId())) {
            throw new IllegalStateException("No tienes permiso para acceder a este grupo.");
        }
    }
}
