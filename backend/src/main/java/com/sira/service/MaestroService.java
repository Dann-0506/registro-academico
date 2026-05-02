package com.sira.service;

import com.sira.model.Maestro;
import com.sira.model.Usuario;
import com.sira.repository.GrupoRepository;
import com.sira.repository.MaestroRepository;
import com.sira.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MaestroService {

    @Autowired private MaestroRepository maestroRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private GrupoRepository grupoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Maestro> listarTodos() {
        return maestroRepository.findAllWithUsuario();
    }

    @Transactional(readOnly = true)
    public Maestro buscarPorId(Integer id) {
        return maestroRepository.findByIdWithUsuario(id)
                .orElseThrow(() -> new NoSuchElementException("Maestro no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Maestro buscarPorUsuarioId(Integer usuarioId) {
        return maestroRepository.findByUsuarioIdWithUsuario(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Perfil de maestro no encontrado para el usuario: " + usuarioId));
    }

    @Transactional(readOnly = true)
    public Maestro buscarPorNumEmpleado(String numEmpleado) {
        return maestroRepository.findByNumEmpleadoWithUsuario(numEmpleado)
                .orElseThrow(() -> new NoSuchElementException("Maestro no encontrado con número de empleado: " + numEmpleado));
    }

    @Transactional
    public Maestro crear(String nombre, String email, String numEmpleado) {
        validarCampos(nombre, email, numEmpleado);
        if (maestroRepository.existsByNumEmpleado(numEmpleado)) {
            throw new IllegalStateException("El número de empleado '" + numEmpleado + "' ya está registrado.");
        }
        if (email != null && !email.isBlank() && usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("El correo electrónico ya está registrado en el sistema.");
        }
        String numEmpleadoNormalizado = numEmpleado.trim().toUpperCase();
        Usuario usuario = new Usuario(nombre.trim(), email != null ? email.trim() : null, passwordEncoder.encode(numEmpleadoNormalizado), "maestro");
        usuario.setRequiereCambioPassword(true);
        usuarioRepository.save(usuario);
        Maestro saved = maestroRepository.save(new Maestro(usuario, numEmpleadoNormalizado));
        return maestroRepository.findByIdWithUsuario(saved.getId()).orElseThrow();
    }

    @Transactional
    public Maestro actualizar(Integer id, String nombre, String email, String numEmpleado) {
        Maestro maestro = buscarPorId(id);
        validarCampos(nombre, email, numEmpleado);

        if (!maestro.getNumEmpleado().equals(numEmpleado) && maestroRepository.existsByNumEmpleado(numEmpleado)) {
            throw new IllegalStateException("El número de empleado '" + numEmpleado + "' ya está registrado.");
        }

        Usuario usuario = maestro.getUsuario();
        if (email != null && !email.equalsIgnoreCase(usuario.getEmail()) && usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("El correo electrónico ya está registrado en el sistema.");
        }

        usuario.setNombre(nombre.trim());
        usuario.setEmail(email != null ? email.trim() : null);
        usuarioRepository.save(usuario);
        maestro.setNumEmpleado(numEmpleado.trim().toUpperCase());
        maestroRepository.save(maestro);
        return maestroRepository.findByIdWithUsuario(id).orElseThrow();
    }

    @Transactional
    public void cambiarEstado(Integer id, boolean activo) {
        Maestro maestro = buscarPorId(id);
        maestro.getUsuario().setActivo(activo);
        usuarioRepository.save(maestro.getUsuario());
    }

    @Transactional
    public void restablecerPassword(Integer id) {
        Maestro maestro = buscarPorId(id);
        maestro.getUsuario().setPasswordHash(passwordEncoder.encode(maestro.getNumEmpleado()));
        maestro.getUsuario().setRequiereCambioPassword(true);
        usuarioRepository.save(maestro.getUsuario());
    }

    @Transactional
    public void eliminar(Integer id) {
        Maestro maestro = buscarPorId(id);
        if (grupoRepository.existsByMaestroId(id)) {
            throw new IllegalStateException("No se puede eliminar: el docente tiene grupos asignados. Usa 'Desactivar'.");
        }
        Integer usuarioId = maestro.getUsuario().getId();
        maestroRepository.delete(maestro);
        usuarioRepository.deleteById(usuarioId);
    }

    private void validarCampos(String nombre, String email, String numEmpleado) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (numEmpleado == null || numEmpleado.isBlank()) {
            throw new IllegalArgumentException("El número de empleado es obligatorio.");
        }
        if (email != null && !email.isBlank() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
        }
    }
}
