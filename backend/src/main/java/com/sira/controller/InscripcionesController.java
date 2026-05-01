package com.sira.controller;

import com.sira.dto.InscripcionRequest;
import com.sira.dto.InscripcionResponse;
import com.sira.service.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inscripciones")
@PreAuthorize("hasRole('ADMIN')")
public class InscripcionesController {

    @Autowired private InscripcionService inscripcionService;

    @GetMapping
    public List<InscripcionResponse> listar() {
        return inscripcionService.listarTodas().stream().map(InscripcionResponse::from).toList();
    }

    @GetMapping("/grupo/{grupoId}")
    public List<InscripcionResponse> listarPorGrupo(@PathVariable Integer grupoId) {
        return inscripcionService.listarPorGrupo(grupoId).stream().map(InscripcionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public InscripcionResponse obtener(@PathVariable Integer id) {
        return InscripcionResponse.from(inscripcionService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<InscripcionResponse> inscribir(@RequestBody InscripcionRequest request) {
        InscripcionResponse response = InscripcionResponse.from(
                inscripcionService.inscribir(request.alumnoId(), request.grupoId(), request.fecha())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        inscripcionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
