package com.academico.service.individuals;

import com.academico.dao.UnidadDAO;
import com.academico.model.Unidad;
import java.sql.SQLException;
import java.util.List;

public class UnidadService {
    private final UnidadDAO unidadDAO = new UnidadDAO();

    public List<Unidad> listarPorGrupo(int grupoId) throws Exception {
        try {
            return unidadDAO.findByGrupo(grupoId);
        } catch (SQLException e) {
            throw new Exception("Error al cargar las unidades del grupo.");
        }
    }
}