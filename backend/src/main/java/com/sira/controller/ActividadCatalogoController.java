package com.sira.controller;

import com.sira.dto.ActividadCatalogoRequest;
import com.sira.dto.ActividadCatalogoResponse;
import com.sira.service.ActividadCatalogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/actividades-catalogo")
@PreAuthorize("hasRole('ADMIN')")
public class ActividadCatalogoController {

    @Autowired private ActividadCatalogoService catalogoService;

    @GetMapping
    public List<ActividadCatalogoResponse> listar() {
        return catalogoService.listarTodas().stream().map(ActividadCatalogoResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<ActividadCatalogoResponse> crear(@RequestBody ActividadCatalogoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ActividadCatalogoResponse.from(
                        catalogoService.crear(request.nombre(), request.descripcion())));
    }

    @PutMapping("/{id}")
    public ActividadCatalogoResponse actualizar(@PathVariable Integer id,
                                                 @RequestBody ActividadCatalogoRequest request) {
        return ActividadCatalogoResponse.from(
                catalogoService.actualizar(id, request.nombre(), request.descripcion()));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Integer id, @RequestParam boolean activo) {
        catalogoService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        catalogoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
