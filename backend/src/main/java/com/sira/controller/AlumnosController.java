package com.sira.controller;

import com.sira.dto.AlumnoRequest;
import com.sira.dto.AlumnoResponse;
import com.sira.service.AlumnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/alumnos")
@PreAuthorize("hasRole('ADMIN')")
public class AlumnosController {

    @Autowired private AlumnoService alumnoService;

    @GetMapping
    public List<AlumnoResponse> listar() {
        return alumnoService.listarTodos().stream().map(AlumnoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AlumnoResponse obtener(@PathVariable Integer id) {
        return AlumnoResponse.from(alumnoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AlumnoResponse> crear(@RequestBody AlumnoRequest request) {
        AlumnoResponse response = AlumnoResponse.from(
                alumnoService.crear(request.nombre(), request.email(), request.matricula())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public AlumnoResponse actualizar(@PathVariable Integer id, @RequestBody AlumnoRequest request) {
        return AlumnoResponse.from(
                alumnoService.actualizar(id, request.nombre(), request.email(), request.matricula())
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Integer id, @RequestParam boolean activo) {
        alumnoService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> restablecerPassword(@PathVariable Integer id) {
        alumnoService.restablecerPassword(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        alumnoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
