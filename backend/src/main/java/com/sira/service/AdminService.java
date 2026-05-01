package com.sira.service;

import com.sira.model.Usuario;
import com.sira.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AdminService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findByRolOrderByNombreAsc("admin");
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Administrador no encontrado con id: " + id));
        if (!"admin".equals(usuario.getRol())) {
            throw new NoSuchElementException("El usuario especificado no es administrador.");
        }
        return usuario;
    }

    @Transactional
    public Usuario crear(String nombre, String email) {
        validarCampos(nombre, email);
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("El correo electrónico ya está registrado en el sistema.");
        }
        return usuarioRepository.save(
                new Usuario(nombre.trim(), email.trim().toLowerCase(), passwordEncoder.encode("123456"), "admin")
        );
    }

    @Transactional
    public Usuario actualizar(Integer id, String nombre, String email, Integer actorId) {
        verificarNoEsMismoUsuario(id, actorId);
        validarCampos(nombre, email);

        Usuario admin = buscarPorId(id);
        if (!admin.getEmail().equalsIgnoreCase(email) && usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("El correo electrónico ya está registrado en el sistema.");
        }

        admin.setNombre(nombre.trim());
        admin.setEmail(email.trim().toLowerCase());
        return usuarioRepository.save(admin);
    }

    @Transactional
    public void cambiarEstado(Integer id, boolean activo, Integer actorId) {
        verificarNoEsMismoUsuario(id, actorId);
        if (!activo) {
            verificarNoEsUltimoAdminActivo(id);
        }
        Usuario admin = buscarPorId(id);
        admin.setActivo(activo);
        usuarioRepository.save(admin);
    }

    @Transactional
    public void restablecerPassword(Integer id, Integer actorId) {
        verificarNoEsMismoUsuario(id, actorId);
        Usuario admin = buscarPorId(id);
        admin.setPasswordHash(passwordEncoder.encode("123456"));
        usuarioRepository.save(admin);
    }

    @Transactional
    public void eliminar(Integer id, Integer actorId) {
        verificarNoEsMismoUsuario(id, actorId);
        verificarNoEsUltimoAdminActivo(id);
        Usuario admin = buscarPorId(id);
        usuarioRepository.delete(admin);
    }

    // ==========================================
    // GUARDIAS DE SEGURIDAD
    // ==========================================

    private void verificarNoEsMismoUsuario(Integer targetId, Integer actorId) {
        if (targetId.equals(actorId)) {
            throw new IllegalStateException("No puedes realizar esta acción sobre tu propia cuenta.");
        }
    }

    private void verificarNoEsUltimoAdminActivo(Integer id) {
        long adminsActivos = usuarioRepository.countByRolAndActivo("admin", true);
        Usuario target = buscarPorId(id);
        if (adminsActivos <= 1 && target.isActivo()) {
            throw new IllegalStateException(
                    "Operación denegada: no puedes eliminar ni desactivar al único administrador activo del sistema.");
        }
    }

    private void validarCampos(String nombre, String email) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
        }
    }
}
