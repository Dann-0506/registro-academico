package com.sira.controller;

import com.sira.dto.GrupoRequest;
import com.sira.dto.GrupoResponse;
import com.sira.service.GrupoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/grupos")
@PreAuthorize("hasRole('ADMIN')")
public class GruposController {

    @Autowired private GrupoService grupoService;

    @GetMapping
    public List<GrupoResponse> listar() {
        return grupoService.listarTodos().stream().map(GrupoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public GrupoResponse obtener(@PathVariable Integer id) {
        return GrupoResponse.from(grupoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<GrupoResponse> crear(@RequestBody GrupoRequest request) {
        GrupoResponse response = GrupoResponse.from(
                grupoService.crear(
                        request.materiaId(), request.maestroId(),
                        request.clave(), request.semestre(),
                        request.calificacionMinimaAprobatoria(), request.calificacionMaxima()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public GrupoResponse actualizar(@PathVariable Integer id, @RequestBody GrupoRequest request) {
        return GrupoResponse.from(
                grupoService.actualizar(id, request.materiaId(), request.maestroId(), request.clave(), request.semestre())
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Integer id, @RequestParam boolean activo) {
        grupoService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<Void> cerrar(@PathVariable Integer id) {
        grupoService.cerrarCurso(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reabrir")
    public ResponseEntity<Void> reabrir(@PathVariable Integer id) {
        grupoService.reabrirCurso(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cerrar-definitivo")
    public ResponseEntity<Void> cerrarDefinitivamente(@PathVariable Integer id) {
        grupoService.cerrarDefinitivamente(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        grupoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
