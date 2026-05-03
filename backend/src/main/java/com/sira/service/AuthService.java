package com.sira.service;

import com.sira.model.Usuario;
import com.sira.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public Optional<Usuario> login(String email, String passwordPlano) {
        return usuarioRepository.findByEmail(email)
                .filter(u -> u.isActivo() && passwordEncoder.matches(passwordPlano, u.getPasswordHash()));
    }

    public String hashearPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public void cambiarPassword(Integer usuarioId, String nuevaPassword) {
        usuarioRepository.actualizarPassword(usuarioId, passwordEncoder.encode(nuevaPassword));
    }
}
