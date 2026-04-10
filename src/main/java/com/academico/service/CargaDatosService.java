package com.academico.service;

import com.academico.model.*;
import com.academico.service.individuals.AlumnoService;
import com.academico.service.individuals.GrupoService;
import com.academico.service.individuals.InscripcionService;
import com.academico.service.individuals.MaestroService;
import com.academico.service.individuals.MateriaService;
import com.academico.util.CsvUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquestador de carga masiva. 
 * Su función es procesar archivos CSV y delegar el guardado a los servicios correspondientes.
 */
public class CargaDatosService {

    // Dependencias de Servicios (Orquestación)
    private final AlumnoService alumnoService;
    private final MateriaService materiaService;
    private final MaestroService maestroService;
    private final GrupoService grupoService;
    private final InscripcionService inscripcionService;

    public CargaDatosService() {
        this.alumnoService = new AlumnoService();
        this.materiaService = new MateriaService();
        this.maestroService = new MaestroService();
        this.grupoService = new GrupoService();
        this.inscripcionService = new InscripcionService();
    }

    public CargaDatosService(AlumnoService alumnoService, MateriaService materiaService, MaestroService maestroService,
                             GrupoService grupoService, InscripcionService inscripcionService) {
        this.alumnoService = alumnoService;
        this.materiaService = materiaService;
        this.maestroService = maestroService;
        this.grupoService = grupoService;
        this.inscripcionService = inscripcionService;
    }

    // === IMPORTACIÓN DE ALUMNOS ===
    public List<String> importarAlumnosCsv(InputStream is) {
        List<String> errores = new ArrayList<>();
        try {
            List<String[]> lineas = CsvUtil.leerCsv(is);
            for (int i = 0; i < lineas.size(); i++) {
                String[] fila = lineas.get(i);
                if (fila.length < 2) continue;

                try {
                    Alumno a = new Alumno();
                    a.setMatricula(fila[0].trim());
                    a.setNombre(fila[1].trim());
                    if (fila.length >= 3) a.setEmail(fila[2].trim());
                    
                    // Delega al servicio la validación y guardado
                    alumnoService.guardar(a, false); 
                } catch (Exception e) {
                    errores.add("Línea " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errores.add("Error crítico al leer el archivo de alumnos: " + e.getMessage());
        }
        return errores;
    }

    // === IMPORTACIÓN DE MATERIAS ===
    public List<String> importarMateriasCsv(InputStream is) {
        List<String> errores = new ArrayList<>();
        try {
            List<String[]> lineas = CsvUtil.leerCsv(is);
            for (int i = 0; i < lineas.size(); i++) {
                String[] fila = lineas.get(i);
                if (fila.length < 3) continue;

                try {
                    Materia m = new Materia();
                    m.setClave(fila[0].trim());
                    m.setNombre(fila[1].trim());
                    m.setTotalUnidades(Integer.parseInt(fila[2].trim()));

                    materiaService.guardar(m);
                } catch (Exception e) {
                    errores.add("Línea " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errores.add("Error crítico al leer el archivo de materias.");
        }
        return errores;
    }

    // === IMPORTACIÓN DE MAESTROS ===
    public List<String> importarMaestrosCsv(InputStream is) {
        List<String> errores = new ArrayList<>();
        try {
            List<String[]> lineas = CsvUtil.leerCsv(is);
            for (int i = 0; i < lineas.size(); i++) {
                String[] fila = lineas.get(i);
                
                // 1. Detección automática de encabezado (Se salta la fila 1 si parece título)
                if (i == 0 && (fila[0].toLowerCase().contains("num") || fila[0].toLowerCase().contains("empleado"))) {
                    continue; 
                }

                // 2. Validación de estructura (Mínimo Identificador y Nombre)
                if (fila.length < 2) {
                    errores.add("Línea " + (i + 1) + ": Faltan columnas obligatorias.");
                    continue;
                }

                try {
                    Maestro m = new Maestro();
                    // Nuevo estándar: ID, NOMBRE, EMAIL
                    m.setNumEmpleado(fila[0].trim());
                    m.setNombre(fila[1].trim());
                    if (fila.length >= 3) m.setEmail(fila[2].trim());

                    maestroService.guardar(m, false);
                } catch (Exception e) {
                    errores.add("Línea " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errores.add("Error crítico: El archivo no tiene un formato CSV válido.");
        }
        return errores;
    }

    // === IMPORTACIÓN DE GRUPOS ===
    public List<String> importarGruposCsv(InputStream is) {
        List<String> errores = new ArrayList<>();
        try {
            List<String[]> lineas = CsvUtil.leerCsv(is);
            for (int i = 0; i < lineas.size(); i++) {
                String[] fila = lineas.get(i);
                if (fila.length < 4) continue;

                try {
                    Grupo g = new Grupo();
                    g.setMateriaId(Integer.parseInt(fila[0].trim()));
                    g.setMaestroId(Integer.parseInt(fila[1].trim()));
                    g.setClave(fila[2].trim());
                    g.setSemestre(fila[3].trim());
                    g.setActivo(true);

                    grupoService.guardar(g);
                } catch (Exception e) {
                    errores.add("Línea " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errores.add("Error crítico al leer el archivo de grupos.");
        }
        return errores;
    }

    // === IMPORTACIÓN DE INSCRIPCIONES ===
    public List<String> importarInscripcionesCsv(InputStream is) {
        List<String> errores = new ArrayList<>();
        try {
            List<String[]> lineas = CsvUtil.leerCsv(is);
            for (int i = 0; i < lineas.size(); i++) {
                String[] fila = lineas.get(i);
                if (fila.length < 2) continue;

                try {
                    Inscripcion ins = new Inscripcion();
                    ins.setAlumnoId(Integer.parseInt(fila[0].trim()));
                    ins.setGrupoId(Integer.parseInt(fila[1].trim()));

                    inscripcionService.inscribir(ins);
                } catch (Exception e) {
                    errores.add("Línea " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errores.add("Error crítico al leer el archivo de inscripciones.");
        }
        return errores;
    }
}