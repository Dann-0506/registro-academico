package com.sira.service;

import com.sira.model.Alumno;
import com.sira.model.Usuario;
import com.sira.repository.AlumnoRepository;
import com.sira.repository.InscripcionRepository;
import com.sira.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AlumnoService {

    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Alumno> listarTodos() {
        return alumnoRepository.findAllWithUsuario();
    }

    @Transactional(readOnly = true)
    public Alumno buscarPorId(Integer id) {
        return alumnoRepository.findByIdWithUsuario(id)
                .orElseThrow(() -> new NoSuchElementException("Alumno no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Alumno buscarPorUsuarioId(Integer usuarioId) {
        return alumnoRepository.findByUsuarioIdWithUsuario(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Perfil de alumno no encontrado para el usuario: " + usuarioId));
    }

    @Transactional(readOnly = true)
    public Alumno buscarPorMatricula(String matricula) {
        return alumnoRepository.findByMatriculaWithUsuario(matricula)
                .orElseThrow(() -> new NoSuchElementException("Alumno no encontrado con matrícula: " + matricula));
    }

    @Transactional(readOnly = true)
    public List<Alumno> buscarPorGrupo(Integer grupoId) {
        return alumnoRepository.findByGrupoId(grupoId);
    }

    @Transactional
    public Alumno crear(String nombre, String email, String matricula) {
        validarCampos(nombre, email, matricula);
        if (alumnoRepository.existsByMatricula(matricula)) {
            throw new IllegalStateException("La matrícula '" + matricula + "' ya está registrada.");
        }
        if (email != null && !email.isBlank() && usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("El correo electrónico ya está registrado en el sistema.");
        }
        Usuario usuario = usuarioRepository.save(
                new Usuario(nombre.trim(), email != null ? email.trim() : null, passwordEncoder.encode("123456"), "alumno")
        );
        Alumno saved = alumnoRepository.save(new Alumno(usuario, matricula.trim().toUpperCase()));
        return alumnoRepository.findByIdWithUsuario(saved.getId()).orElseThrow();
    }

    @Transactional
    public Alumno actualizar(Integer id, String nombre, String email, String matricula) {
        Alumno alumno = buscarPorId(id);
        validarCampos(nombre, email, matricula);

        if (!alumno.getMatricula().equals(matricula) && alumnoRepository.existsByMatricula(matricula)) {
            throw new IllegalStateException("La matrícula '" + matricula + "' ya está registrada.");
        }

        Usuario usuario = alumno.getUsuario();
        if (email != null && !email.equalsIgnoreCase(usuario.getEmail()) && usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("El correo electrónico ya está registrado en el sistema.");
        }

        usuario.setNombre(nombre.trim());
        usuario.setEmail(email != null ? email.trim() : null);
        usuarioRepository.save(usuario);
        alumno.setMatricula(matricula.trim().toUpperCase());
        alumnoRepository.save(alumno);
        return alumnoRepository.findByIdWithUsuario(alumno.getId()).orElseThrow();
    }

    @Transactional
    public void cambiarEstado(Integer id, boolean activo) {
        Alumno alumno = buscarPorId(id);
        alumno.getUsuario().setActivo(activo);
        usuarioRepository.save(alumno.getUsuario());
    }

    @Transactional
    public void restablecerPassword(Integer id) {
        Alumno alumno = buscarPorId(id);
        alumno.getUsuario().setPasswordHash(passwordEncoder.encode("123456"));
        usuarioRepository.save(alumno.getUsuario());
    }

    @Transactional
    public void eliminar(Integer id) {
        Alumno alumno = buscarPorId(id);
        if (inscripcionRepository.existsByAlumnoId(id)) {
            throw new IllegalStateException("No se puede eliminar: el alumno tiene registros académicos. Usa 'Desactivar'.");
        }
        Integer usuarioId = alumno.getUsuario().getId();
        alumnoRepository.delete(alumno);
        usuarioRepository.deleteById(usuarioId);
    }

    private void validarCampos(String nombre, String email, String matricula) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("La matrícula es obligatoria.");
        }
        if (email != null && !email.isBlank() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
        }
    }
}
