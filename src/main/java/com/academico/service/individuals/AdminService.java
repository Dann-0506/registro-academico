package com.academico.service.individuals;

import com.academico.dao.AdminDAO;
import com.academico.model.Usuario;
import com.academico.service.AuthService;

import java.sql.SQLException;
import java.util.List;

public class AdminService {
    private final AdminDAO adminDAO = new AdminDAO();
    private final AuthService authService = new AuthService();

    public List<Usuario> listarAdmins() throws Exception {
        try {
            return adminDAO.findAllAdmins();
        } catch (SQLException e) {
            throw new Exception("Error al cargar la lista de administradores.");
        }
    }

    public void guardar(Usuario admin, boolean esEdicion) throws Exception {
        if (admin.getNombre().isBlank() || admin.getEmail().isBlank()) {
            throw new IllegalArgumentException("El nombre y el correo son obligatorios.");
        }
        
        if (!admin.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo es inválido.");
        }

        try {
            if (esEdicion) {
                adminDAO.actualizar(admin);
            } else {
                // Contraseña genérica por defecto, como en los otros catálogos
                String hashSeguro = authService.hashearPassword("123456");
                adminDAO.crear(admin, hashSeguro);
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState()) && e.getMessage().contains("email")) {
                throw new Exception("El correo ya está registrado.");
            }
            throw new Exception("Error al guardar el administrador.");
        }
    }

    public void cambiarEstado(int id, boolean estado) throws Exception {
        try {
            adminDAO.cambiarEstado(id, estado);
        } catch (SQLException e) {
            throw new Exception("Error al cambiar el estado del administrador.");
        }
    }

    public void eliminar(int id) throws Exception {
        try {
            adminDAO.eliminar(id);
        } catch (SQLException e) {
            throw new Exception("Error al eliminar el administrador.");
        }
    }

    public void restablecerPassword(int id) throws Exception {
        try {
            // Reutilizamos authService para encriptar la contraseña genérica
            String hashSeguro = authService.hashearPassword("123456");
            adminDAO.actualizarPassword(id, hashSeguro);
        } catch (SQLException e) {
            throw new Exception("Error al restablecer la contraseña.");
        }
    }
}