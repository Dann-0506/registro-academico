package com.academico.service.individuals;

import com.academico.dao.ResultadoDAO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultadoServiceTest {

    @Mock private ResultadoDAO resultadoDAO;
    @Mock private EstadoUnidadService estadoUnidadService;

    @InjectMocks
    private ResultadoService resultadoService;

    @Test
    @DisplayName("Debe guardar calificación si la unidad está abierta")
    void testGuardarCalificacion_Exito() throws Exception {
        BigDecimal nota = new BigDecimal("8.5");
        
        resultadoService.guardarCalificacion(1, 10, 100, 5, nota);

        // Verifica que primero consultó si la unidad estaba abierta
        verify(estadoUnidadService).validarUnidadAbierta(10, 100);
        verify(resultadoDAO).guardar(1, 5, nota);
    }

    @Test
    @DisplayName("Debe fallar al guardar si la unidad está cerrada")
    void testGuardarCalificacion_UnidadCerrada() throws Exception {
        doThrow(new IllegalStateException("Unidad cerrada."))
                .when(estadoUnidadService).validarUnidadAbierta(anyInt(), anyInt());

        assertThrows(IllegalStateException.class, () -> {
            resultadoService.guardarCalificacion(1, 10, 100, 5, BigDecimal.TEN);
        });

        // Asegura que nunca se llamó al DAO
        verify(resultadoDAO, never()).guardar(anyInt(), anyInt(), any());
    }
}