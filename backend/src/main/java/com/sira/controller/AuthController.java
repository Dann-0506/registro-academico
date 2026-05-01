package com.sira.controller;

import com.sira.dto.LoginRequest;
import com.sira.dto.LoginResponse;
import com.sira.security.JwtUtil;
import com.sira.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword())
                .map(usuario -> {
                    String token = jwtUtil.generarToken(usuario);
                    return ResponseEntity.ok(new LoginResponse(
                            token,
                            usuario.getId(),
                            usuario.getNombre(),
                            usuario.getEmail(),
                            usuario.getRol()
                    ));
                })
                .orElse(ResponseEntity.status(401).build());
    }
}
