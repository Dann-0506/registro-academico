package com.academico.service.individuals;

import com.academico.dao.MateriaDAO;
import com.academico.model.Materia;
import java.sql.SQLException;
import java.util.List;

public class MateriaService {
    private final MateriaDAO materiaDAO;

    public MateriaService(){
        this.materiaDAO = new MateriaDAO();
    }

    public MateriaService(MateriaDAO materiaDAO) {
        this.materiaDAO = materiaDAO;
    }

    public List<Materia> listarTodas() throws Exception {
        try { return materiaDAO.findAll(); } 
        catch (SQLException e) { throw new Exception("Error al cargar materias."); }
    }

    public void guardar(Materia materia) throws Exception {
        if (materia.getClave().isBlank() || materia.getNombre().isBlank()) {
            throw new IllegalArgumentException("La clave y el nombre de la materia son obligatorios.");
        }
        if (materia.getTotalUnidades() <= 0) throw new Exception("Mínimo 1 unidad por materia.");
        try {
            materiaDAO.insertar(materia);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) throw new Exception("La clave de materia ya existe.");
            throw new Exception("Error al guardar la materia.");
        }
    }
}