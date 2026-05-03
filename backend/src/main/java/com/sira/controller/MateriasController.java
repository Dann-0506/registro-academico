package com.sira.controller;

import com.sira.dto.MateriaRequest;
import com.sira.dto.MateriaResponse;
import com.sira.service.MateriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/materias")
@PreAuthorize("hasRole('ADMIN')")
public class MateriasController {

    @Autowired private MateriaService materiaService;

    @GetMapping
    public List<MateriaResponse> listar() {
        return materiaService.listarTodas().stream().map(MateriaResponse::from).toList();
    }

    @GetMapping("/{id}")
    public MateriaResponse obtener(@PathVariable Integer id) {
        return MateriaResponse.from(materiaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<MateriaResponse> crear(@RequestBody MateriaRequest request) {
        MateriaResponse response = MateriaResponse.from(
                materiaService.crear(request.clave(), request.nombre(), request.totalUnidades(), request.nombresUnidades())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public MateriaResponse actualizar(@PathVariable Integer id, @RequestBody MateriaRequest request) {
        return MateriaResponse.from(materiaService.actualizar(id, request.nombre()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        materiaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
