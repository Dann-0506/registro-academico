package com.academico.service;

import com.academico.dao.AlumnoDAO;
import com.academico.model.Alumno;
import java.sql.SQLException;
import java.util.List;

public class AlumnoService {
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    public List<Alumno> listarTodos() throws Exception {
        try {
            return alumnoDAO.obtenerTodos();
        } catch (SQLException e) {
            throw new Exception("No se pudieron cargar los alumnos de la base de datos.");
        }
    }

    public void guardar(Alumno alumno, boolean esEdicion) throws Exception {
        // Validación de negocio previa
        if (alumno.getMatricula().isBlank() || alumno.getNombre().isBlank()) {
            throw new IllegalArgumentException("Campos obligatorios faltantes.");
        }

        try {
            if (esEdicion) {
                alumnoDAO.actualizar(alumno);
            } else {
                alumnoDAO.crear(alumno);
            }
        } catch (SQLException e) {
            // TRADUCCIÓN DE ERRORES DE POSTGRES
            String state = e.getSQLState();
            if ("23505".equals(state)) { // Unique violation
                if (e.getMessage().contains("matricula")) throw new Exception("La matrícula ya existe.");
                if (e.getMessage().contains("email")) throw new Exception("El correo ya está registrado.");
            }
            throw new Exception("Error al procesar la solicitud en la base de datos.");
        }
    }

    public void eliminar(int id) throws Exception {
        try {
            alumnoDAO.eliminar(id);
        } catch (SQLException e) {
            // Manejo de Integridad Referencial
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("No se puede eliminar: El alumno tiene registros académicos vinculados.");
            }
            throw new Exception("Error al eliminar el registro.");
        }
    }
}