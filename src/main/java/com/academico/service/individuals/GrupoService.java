package com.academico.service.individuals;

import com.academico.dao.GrupoDAO;
import com.academico.model.Grupo;
import java.sql.SQLException;
import java.util.List;

public class GrupoService {
    private final GrupoDAO grupoDAO;

    public GrupoService() {
        this.grupoDAO = new GrupoDAO();
    }

    public GrupoService(GrupoDAO grupoDAO) {
        this.grupoDAO = grupoDAO;
    }

    public List<Grupo> listarTodos() throws Exception {
        try { return grupoDAO.findAll(); } 
        catch (SQLException e) { throw new Exception("Error al obtener grupos."); }
    }

    public void guardar(Grupo grupo) throws Exception {
        try {
            grupoDAO.insertar(grupo);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) throw new Exception("La clave de grupo ya existe.");
            if ("23503".equals(e.getSQLState())) throw new Exception("Materia o Maestro no encontrados.");
            throw new Exception("Error al crear el grupo.");
        }
    }
}