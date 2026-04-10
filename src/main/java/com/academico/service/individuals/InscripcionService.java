package com.academico.service.individuals;

import com.academico.dao.InscripcionDAO;
import com.academico.model.Inscripcion;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class InscripcionService {
    private final InscripcionDAO inscripcionDAO;

    public InscripcionService() {
        this.inscripcionDAO = new InscripcionDAO();
    }

    public InscripcionService(InscripcionDAO inscripcionDAO) {
        this.inscripcionDAO = inscripcionDAO;
    }

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

    public void aplicarOverrideMateria(int inscripcionId, BigDecimal calificacionManual, String justificacion) throws SQLException {
        @SuppressWarnings("unused")
        Inscripcion inscripcion = inscripcionDAO.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("La inscripción no existe."));
        
        inscripcionDAO.actualizarOverride(inscripcionId, calificacionManual, justificacion);
    }
}