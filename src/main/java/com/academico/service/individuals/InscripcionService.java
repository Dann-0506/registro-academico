package com.academico.service.individuals;

import com.academico.dao.InscripcionDAO;
import com.academico.model.Inscripcion;
import java.sql.SQLException;
import java.util.List;

public class InscripcionService {
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    /**
     * Registra un alumno en un grupo.
     * Valida duplicados mediante el código de error de Postgres.
     */
    public void inscribir(Inscripcion inscripcion) throws Exception {
        try {
            inscripcionDAO.insertar(inscripcion);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new Exception("El alumno ya se encuentra inscrito en este grupo.");
            }
            throw new Exception("Error al procesar la inscripción.");
        }
    }

    /**
     * Función requerida por ReporteService para obtener los integrantes de un grupo.
     */
    public List<Inscripcion> listarPorGrupo(int grupoId) throws Exception {
        try {
            return inscripcionDAO.findByGrupo(grupoId);
        } catch (SQLException e) {
            throw new Exception("Error al obtener la lista de inscritos.");
        }
    }

    /**
     * Permite dar de baja a un alumno de un grupo.
     */
    public void eliminar(int id) throws Exception {
        try {
            inscripcionDAO.eliminar(id);
        } catch (SQLException e) {
            throw new Exception("No se pudo eliminar la inscripción.");
        }
    }
}