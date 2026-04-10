package com.academico.service.individuals;

import com.academico.dao.BonusDAO;
import com.academico.model.Bonus;
import java.sql.SQLException;
import java.util.Optional;

public class BonusService {
    private final BonusDAO bonusDAO = new BonusDAO();

    public Optional<Bonus> obtenerBonusUnidad(int inscripcionId, int unidadId) throws Exception {
        try {
            return bonusDAO.findByInscripcionYUnidad(inscripcionId, unidadId);
        } catch (SQLException e) {
            throw new Exception("Error al consultar bonus de unidad.");
        }
    }

    public Optional<Bonus> obtenerBonusMateria(int inscripcionId) throws Exception {
        try {
            return bonusDAO.findBonusMateria(inscripcionId);
        } catch (SQLException e) {
            throw new Exception("Error al consultar bonus de materia.");
        }
    }
}