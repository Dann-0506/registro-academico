package com.sira.controller;

import com.sira.dto.AdminRequest;
import com.sira.dto.AdminResponse;
import com.sira.model.Usuario;
import com.sira.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/administradores")
@PreAuthorize("hasRole('ADMIN')")
public class AdministradoresController {

    @Autowired private AdminService adminService;

    @GetMapping
    public List<AdminResponse> listar() {
        return adminService.listarTodos().stream().map(AdminResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AdminResponse obtener(@PathVariable Integer id) {
        return AdminResponse.from(adminService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AdminResponse> crear(@RequestBody AdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdminResponse.from(adminService.crear(request.nombre(), request.email(), request.numEmpleado())));
    }

    @PutMapping("/{id}")
    public AdminResponse actualizar(@PathVariable Integer id,
                                    @RequestBody AdminRequest request,
                                    @AuthenticationPrincipal Usuario usuario) {
        return AdminResponse.from(adminService.actualizar(
                id, request.nombre(), request.email(), request.numEmpleado(), usuario.getId()));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Integer id,
                                               @RequestParam boolean activo,
                                               @AuthenticationPrincipal Usuario usuario) {
        adminService.cambiarEstado(id, activo, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> restablecerPassword(@PathVariable Integer id,
                                                      @AuthenticationPrincipal Usuario usuario) {
        adminService.restablecerPassword(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id,
                                          @AuthenticationPrincipal Usuario usuario) {
        adminService.eliminar(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
