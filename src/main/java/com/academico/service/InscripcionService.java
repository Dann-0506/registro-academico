package com.academico.service;

import com.academico.dao.InscripcionDAO;
import com.academico.model.Inscripcion;
import java.sql.SQLException;

public class InscripcionService {
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

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
}