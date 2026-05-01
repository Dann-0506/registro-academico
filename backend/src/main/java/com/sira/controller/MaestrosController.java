package com.sira.controller;

import com.sira.dto.MaestroRequest;
import com.sira.dto.MaestroResponse;
import com.sira.service.MaestroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/maestros")
@PreAuthorize("hasRole('ADMIN')")
public class MaestrosController {

    @Autowired private MaestroService maestroService;

    @GetMapping
    public List<MaestroResponse> listar() {
        return maestroService.listarTodos().stream().map(MaestroResponse::from).toList();
    }

    @GetMapping("/{id}")
    public MaestroResponse obtener(@PathVariable Integer id) {
        return MaestroResponse.from(maestroService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<MaestroResponse> crear(@RequestBody MaestroRequest request) {
        MaestroResponse response = MaestroResponse.from(
                maestroService.crear(request.nombre(), request.email(), request.numEmpleado())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public MaestroResponse actualizar(@PathVariable Integer id, @RequestBody MaestroRequest request) {
        return MaestroResponse.from(
                maestroService.actualizar(id, request.nombre(), request.email(), request.numEmpleado())
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Integer id, @RequestParam boolean activo) {
        maestroService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> restablecerPassword(@PathVariable Integer id) {
        maestroService.restablecerPassword(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        maestroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
