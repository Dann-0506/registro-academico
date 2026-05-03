package com.sira.service;

import com.sira.dto.PerfilResponse;
import com.sira.model.Usuario;
import com.sira.repository.AdministradorRepository;
import com.sira.repository.AlumnoRepository;
import com.sira.repository.MaestroRepository;
import com.sira.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AdministradorRepository administradorRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private MaestroRepository maestroRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PerfilResponse obtenerPerfil(Usuario usuario) {
        String identificador = switch (usuario.getRol()) {
            case "admin"   -> administradorRepository.findByUsuarioId(usuario.getId())
                                .map(a -> a.getNumEmpleado())
                                .orElse(null);
            case "maestro" -> maestroRepository.findByUsuarioId(usuario.getId())
                                .map(m -> m.getNumEmpleado())
                                .orElse(null);
            case "alumno"  -> alumnoRepository.findByUsuarioId(usuario.getId())
                                .map(a -> a.getMatricula())
                                .orElse(null);
            default        -> null;
        };
        return PerfilResponse.of(usuario, identificador);
    }

    @Transactional
    public void cambiarPassword(Integer usuarioId, String actual, String nueva, String confirmar) {
        if (nueva == null || nueva.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía.");
        }
        if (nueva.length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
        }
        if (!nueva.equals(confirmar)) {
            throw new IllegalArgumentException("La confirmación no coincide con la nueva contraseña.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalStateException("Sesión inválida."));

        if (!passwordEncoder.matches(actual, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        usuario.setPasswordHash(passwordEncoder.encode(nueva));
        usuario.setRequiereCambioPassword(false);
        usuarioRepository.save(usuario);
    }
}
