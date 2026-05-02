package com.sira.service;

import com.opencsv.CSVReader;
import com.sira.dto.CargaResultadoResponse;
import com.sira.dto.CargaResultadoResponse.ErrorLinea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CargaDatosService {

    @Autowired private AlumnoService alumnoService;
    @Autowired private MateriaService materiaService;
    @Autowired private MaestroService maestroService;
    @Autowired private AdminService adminService;
    @Autowired private GrupoService grupoService;
    @Autowired private InscripcionService inscripcionService;

    public CargaResultadoResponse importarAlumnos(MultipartFile archivo) {
        return procesar(archivo, (fila, num) -> {
            if (fila.length < 3 || fila[2].isBlank()) throw new IllegalArgumentException("Faltan columnas (Núm. de control, Nombre, Correo).");
            alumnoService.crear(fila[1].trim(), fila[2].trim(), fila[0].trim());
        });
    }

    public CargaResultadoResponse importarAdministradores(MultipartFile archivo) {
        return procesar(archivo, (fila, num) -> {
            if (fila.length < 3) throw new IllegalArgumentException("Faltan columnas (Num. Empleado, Nombre, Correo).");
            adminService.crear(fila[1].trim(), fila[2].trim(), fila[0].trim());
        });
    }

    public CargaResultadoResponse importarMaestros(MultipartFile archivo) {
        return procesar(archivo, (fila, num) -> {
            if (fila.length < 3 || fila[2].isBlank()) throw new IllegalArgumentException("Faltan columnas (Num. Empleado, Nombre, Correo).");
            maestroService.crear(fila[1].trim(), fila[2].trim(), fila[0].trim());
        });
    }

    public CargaResultadoResponse importarMaterias(MultipartFile archivo) {
        return procesar(archivo, (fila, num) -> {
            if (fila.length < 3) throw new IllegalArgumentException("Faltan columnas (Clave, Nombre, Unidades).");
            int totalUnidades = Integer.parseInt(fila[2].trim());
            List<String> nombres = fila.length > 3 && !fila[3].isBlank()
                    ? Arrays.asList(fila[3].split("\\|")) : null;
            materiaService.crear(fila[0].trim(), fila[1].trim(), totalUnidades, nombres);
        });
    }

    public CargaResultadoResponse importarGrupos(MultipartFile archivo) {
        return procesar(archivo, (fila, num) -> {
            if (fila.length < 4) throw new IllegalArgumentException("Faltan columnas (Materia, Docente, Clave, Semestre).");
            Integer materiaId = materiaService.buscarPorClave(fila[0].trim()).getId();
            Integer maestroId = maestroService.buscarPorNumEmpleado(fila[1].trim()).getId();
            grupoService.crear(materiaId, maestroId, fila[2].trim(), fila[3].trim(), null, null);
        });
    }

    public CargaResultadoResponse importarInscripciones(MultipartFile archivo) {
        return procesar(archivo, (fila, num) -> {
            if (fila.length < 3) throw new IllegalArgumentException("Faltan columnas (Matrícula, Clave Grupo, Semestre).");
            Integer alumnoId = alumnoService.buscarPorMatricula(fila[0].trim()).getId();
            Integer grupoId = grupoService.buscarPorClaveYSemestre(fila[1].trim(), fila[2].trim()).getId();
            inscripcionService.inscribir(alumnoId, grupoId, LocalDate.now());
        });
    }

    // ==========================================
    // MOTOR DE PROCESAMIENTO GENÉRICO
    // ==========================================

    @FunctionalInterface
    private interface ProcesadorFila {
        void procesar(String[] fila, int numLinea) throws Exception;
    }

    private CargaResultadoResponse procesar(MultipartFile archivo, ProcesadorFila procesador) {
        List<ErrorLinea> errores = new ArrayList<>();
        int procesados = 0;
        int exitosos = 0;

        try (CSVReader reader = new CSVReader(new InputStreamReader(archivo.getInputStream()))) {
            String[] fila;
            int numLinea = 0;
            while ((fila = reader.readNext()) != null) {
                numLinea++;
                if (numLinea == 1 && esEncabezado(fila)) continue;
                procesados++;
                try {
                    procesador.procesar(fila, numLinea);
                    exitosos++;
                } catch (Exception e) {
                    errores.add(new ErrorLinea(numLinea, e.getMessage()));
                }
            }
        } catch (Exception e) {
            errores.add(new ErrorLinea(0, "Error crítico al leer el archivo: " + e.getMessage()));
        }

        return new CargaResultadoResponse(procesados, exitosos, errores);
    }

    private boolean esEncabezado(String[] fila) {
        if (fila == null || fila.length == 0 || fila[0] == null) return false;
        String primera = fila[0].trim().toLowerCase();
        return primera.matches(".*\\b(matricula|mat|alumno|num|empleado|clave|materia|docente|grupo|admin)\\b.*");
    }
}
