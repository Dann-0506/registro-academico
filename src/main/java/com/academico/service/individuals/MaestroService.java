package com.academico.service.individuals;

import com.academico.dao.MaestroDAO;
import com.academico.model.Maestro;
import java.sql.SQLException;
import java.util.List;

public class MaestroService {
    private final MaestroDAO maestroDAO = new MaestroDAO();

    public List<Maestro> listarTodos() throws Exception {
        try { return maestroDAO.findAll(); } 
        catch (SQLException e) { throw new Exception("Error al cargar la lista de maestros."); }
    }

    public void guardar(Maestro maestro) throws Exception {
        try {
            maestroDAO.crear(maestro);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                if (e.getMessage().contains("num_empleado")) throw new Exception("El número de empleado ya existe.");
                if (e.getMessage().contains("email")) throw new Exception("El correo ya está registrado.");
            }
            throw new Exception("Error al registrar al docente.");
        }
    }
}