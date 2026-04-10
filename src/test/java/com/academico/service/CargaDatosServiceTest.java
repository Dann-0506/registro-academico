package com.academico.service;

import com.academico.model.Alumno;
import com.academico.model.Materia;
import com.academico.service.individuals.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargaDatosServiceTest {

    // Ahora mockeamos SERVICIOS
    @Mock private AlumnoService alumnoService;
    @Mock private MateriaService materiaService;
    @Mock private MaestroService maestroService;
    @Mock private GrupoService grupoService;
    @Mock private InscripcionService inscripcionService;

    @InjectMocks
    private CargaDatosService cargaDatosService;

    private InputStream crearCsvStream(String contenido) {
        return new ByteArrayInputStream(contenido.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Debe delegar el guardado de alumnos al AlumnoService")
    void testImportarAlumnosCsv_Exito() throws Exception {
        String csvFalso = "A001, Juan Perez, juan@test.com\nA002, Ana Gomez";
        InputStream is = crearCsvStream(csvFalso);

        cargaDatosService.importarAlumnosCsv(is);

        // Verificamos que se llamó al servicio, no al DAO
        verify(alumnoService, times(2)).guardar(any(Alumno.class), eq(false));
    }

    @Test
    @DisplayName("Debe recolectar errores cuando el servicio de materias lanza una excepción")
    void testImportarMateriasCsv_ManejoErrores() throws Exception {
        String csvFalso = "MAT1, Fisica, 5";
        InputStream is = crearCsvStream(csvFalso);

        // Simulamos que el servicio detecta un error (ej. clave duplicada)
        doThrow(new Exception("La clave ya existe")).when(materiaService).guardar(any(Materia.class));

        List<String> errores = cargaDatosService.importarMateriasCsv(is);

        assertEquals(1, errores.size());
        assertEquals("Línea 1: La clave ya existe", errores.get(0));
    }
}