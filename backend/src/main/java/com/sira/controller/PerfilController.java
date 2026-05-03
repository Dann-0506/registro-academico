package com.sira.controller;

import com.sira.dto.CambiarPasswordRequest;
import com.sira.dto.PerfilResponse;
import com.sira.model.Usuario;
import com.sira.service.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    @Autowired private PerfilService perfilService;

    @GetMapping
    public PerfilResponse obtener(@AuthenticationPrincipal Usuario usuario) {
        return perfilService.obtenerPerfil(usuario);
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(@RequestBody CambiarPasswordRequest request,
                                                 @AuthenticationPrincipal Usuario usuario) {
        perfilService.cambiarPassword(
                usuario.getId(),
                request.passwordActual(),
                request.passwordNueva(),
                request.passwordConfirmar()
        );
        return ResponseEntity.noContent().build();
    }
}
