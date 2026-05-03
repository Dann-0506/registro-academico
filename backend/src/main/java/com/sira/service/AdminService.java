package com.sira.service;

import com.sira.model.Administrador;
import com.sira.model.Usuario;
import com.sira.repository.AdministradorRepository;
import com.sira.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AdminService {

    @Autowired private AdministradorRepository administradorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Administrador> listarTodos() {
        return administradorRepository.findAllWithUsuario();
    }

    @Transactional(readOnly = true)
    public Administrador buscarPorId(Integer id) {
        return administradorRepository.findByIdWithUsuario(id)
                .orElseThrow(() -> new NoSuchElementException("Administrador no encontrado con id: " + id));
    }

    @Transactional
    public Administrador crear(String nombre, String email, String numEmpleado) {
        validarCampos(nombre, email, numEmpleado);
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("El correo electrónico ya está registrado en el sistema.");
        }
        if (administradorRepository.existsByNumEmpleado(numEmpleado)) {
            throw new IllegalStateException("El número de empleado '" + numEmpleado + "' ya está registrado.");
        }
        String numEmpleadoNormalizado = numEmpleado.trim().toUpperCase();
        Usuario usuario = new Usuario(nombre.trim(), email.trim().toLowerCase(), passwordEncoder.encode(numEmpleadoNormalizado), "admin");
        usuario.setRequiereCambioPassword(true);
        usuarioRepository.save(usuario);
        Administrador saved = administradorRepository.save(new Administrador(usuario, numEmpleadoNormalizado));
        return administradorRepository.findByIdWithUsuario(saved.getId()).orElseThrow();
    }

    @Transactional
    public Administrador actualizar(Integer id, String nombre, String email, String numEmpleado, Integer actorId) {
        Administrador admin = buscarPorId(id);
        verificarNoEsMismoUsuario(admin, actorId);
        validarCampos(nombre, email, numEmpleado);

        if (!admin.getUsuario().getEmail().equalsIgnoreCase(email) && usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("El correo electrónico ya está registrado en el sistema.");
        }
        if (!admin.getNumEmpleado().equals(numEmpleado) && administradorRepository.existsByNumEmpleado(numEmpleado)) {
            throw new IllegalStateException("El número de empleado '" + numEmpleado + "' ya está registrado.");
        }

        admin.getUsuario().setNombre(nombre.trim());
        admin.getUsuario().setEmail(email.trim().toLowerCase());
        usuarioRepository.save(admin.getUsuario());
        admin.setNumEmpleado(numEmpleado.trim().toUpperCase());
        administradorRepository.save(admin);
        return administradorRepository.findByIdWithUsuario(id).orElseThrow();
    }

    @Transactional
    public void cambiarEstado(Integer id, boolean activo, Integer actorId) {
        Administrador admin = buscarPorId(id);
        verificarNoEsMismoUsuario(admin, actorId);
        if (!activo) {
            verificarNoEsUltimoAdminActivo(admin);
        }
        admin.getUsuario().setActivo(activo);
        usuarioRepository.save(admin.getUsuario());
    }

    @Transactional
    public void restablecerPassword(Integer id, Integer actorId) {
        Administrador admin = buscarPorId(id);
        verificarNoEsMismoUsuario(admin, actorId);
        admin.getUsuario().setPasswordHash(passwordEncoder.encode(admin.getNumEmpleado()));
        admin.getUsuario().setRequiereCambioPassword(true);
        usuarioRepository.save(admin.getUsuario());
    }

    @Transactional
    public void eliminar(Integer id, Integer actorId) {
        Administrador admin = buscarPorId(id);
        verificarNoEsMismoUsuario(admin, actorId);
        verificarNoEsUltimoAdminActivo(admin);
        Integer usuarioId = admin.getUsuario().getId();
        administradorRepository.delete(admin);
        usuarioRepository.deleteById(usuarioId);
    }

    // ==========================================
    // GUARDIAS DE SEGURIDAD
    // ==========================================

    private void verificarNoEsMismoUsuario(Administrador target, Integer actorId) {
        if (target.getUsuario().getId().equals(actorId)) {
            throw new IllegalStateException("No puedes realizar esta acción sobre tu propia cuenta.");
        }
    }

    private void verificarNoEsUltimoAdminActivo(Administrador target) {
        long adminsActivos = usuarioRepository.countByRolAndActivo("admin", true);
        if (adminsActivos <= 1 && target.getUsuario().isActivo()) {
            throw new IllegalStateException(
                    "Operación denegada: no puedes eliminar ni desactivar al único administrador activo del sistema.");
        }
    }

    private void validarCampos(String nombre, String email, String numEmpleado) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
        }
        if (numEmpleado == null || numEmpleado.isBlank()) {
            throw new IllegalArgumentException("El número de empleado es obligatorio.");
        }
    }
}
