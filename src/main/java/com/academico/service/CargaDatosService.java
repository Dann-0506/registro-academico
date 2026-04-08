package com.academico.service;

import com.academico.dao.AlumnoDAO;
import com.academico.dao.MateriaDAO;
import com.academico.model.Alumno;
import com.academico.model.Materia;
import com.academico.util.CsvUtil;
import com.opencsv.exceptions.CsvValidationException;

import java.io.InputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CargaDatosService {

    private final AlumnoDAO alumnoDAO;
    private final MateriaDAO materiaDAO;

    public CargaDatosService() {
        this.alumnoDAO = new AlumnoDAO();
        this.materiaDAO = new MateriaDAO();
    }

    public List<String> importarAlumnosCsv(InputStream is) throws IOException, CsvValidationException, SQLException {
        List<String[]> lineas = CsvUtil.leerCsv(is);
        List<Alumno> alumnos = new ArrayList<>();

        for (String[] fila : lineas) {
            // Ignorar filas vacías o mal formateadas
            if (fila.length >= 2) {
                Alumno alumno = new Alumno();
                alumno.setMatricula(fila[0].trim());
                alumno.setNombre(fila[1].trim());
                
                if (fila.length >= 3 && !fila[2].trim().isEmpty()) {
                    alumno.setEmail(fila[2].trim());
                }
                
                alumnos.add(alumno);
            }
        }

        return alumnoDAO.insertarLote(alumnos);
    }

    public List<String> importarMateriasCsv(InputStream is) throws IOException, CsvValidationException, SQLException {
        List<String[]> lineas = CsvUtil.leerCsv(is);
        List<Materia> materias = new ArrayList<>();

        for (String[] fila : lineas) {
            if (fila.length >= 3) {
                Materia materia = new Materia();
                materia.setClave(fila[0].trim());
                materia.setNombre(fila[1].trim());
                
                try {
                    materia.setTotalUnidades(Integer.parseInt(fila[2].trim()));
                    materias.add(materia);
                } catch (NumberFormatException e) {
                    System.err.println("Error de formato en total de unidades para la materia: " + materia.getClave());
                }
            }
        }

        return materiaDAO.insertarLote(materias);
    }
}