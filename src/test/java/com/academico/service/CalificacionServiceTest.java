package com.academico.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.academico.model.ActividadGrupo;
import com.academico.model.Alumno;
import com.academico.model.CalificacionFinal;
import com.academico.model.Resultado;
import com.academico.model.ResultadoUnidad;
import com.academico.model.Unidad;

class CalificacionServiceTest {

    private CalificacionService service;

    // Constantes para simular los límites históricos del grupo en el test
    private static final BigDecimal LIMITE_MINIMO = new BigDecimal("70.00");
    private static final BigDecimal LIMITE_MAXIMO = new BigDecimal("100.00");

    @BeforeEach
    void setUp() {
        // La instanciación ahora es directa y limpia
        service = new CalificacionService();
    }

    // ── Pruebas de Ponderaciones (Sin cambios en firmas) ─────────────────────

    @Test
    @DisplayName("Suma correcta de ponderaciones")
    void sumarPonderaciones_valido() {
        List<ActividadGrupo> acts = Arrays.asList(actividad(30), actividad(70));
        assertEquals(new BigDecimal("100.00"), service.sumarPonderaciones(acts));
    }

    @Test
    @DisplayName("Ponderación válida es exactamente 100")
    void ponderacionesValidas_verdadero() {
        List<ActividadGrupo> acts = Arrays.asList(actividad(50), actividad(50));
        assertTrue(service.ponderacionesValidas(acts));
    }

    // ── Pruebas de Resultados y Bonus ────────────────────────────────────────

    @Test
    @DisplayName("Aplicar bonus respeta el límite máximo del grupo")
    void aplicarBonusUnidad_respetaLimite() {
        // Tiene 95, le dan 10 de bonus. El límite es 100. No debe pasar de 100.
        BigDecimal resultado = service.aplicarBonusUnidad(
            new BigDecimal("95.00"), 
            new BigDecimal("10.00"), 
            LIMITE_MAXIMO
        );
        assertEquals(new BigDecimal("100.00"), resultado);
    }

    @Test
    @DisplayName("Calcular Resultado Base correctamente")
    void calcularResultadoBase_correcto() {
        List<Resultado> res = Arrays.asList(resultado(100, 50), resultado(80, 50));
        // 50% de 100 = 50. 50% de 80 = 40. Total = 90.
        assertEquals(new BigDecimal("90.00"), service.calcularResultadoBase(res));
    }

    @Test
    @DisplayName("Calcular Resultado Unidad completo con bonus")
    void calcularResultadoUnidad_completo() throws Exception {
        Unidad u = new Unidad(); u.setId(1);
        List<Resultado> res = Arrays.asList(resultado(80, 100)); // Base 80
        
        ResultadoUnidad ru = service.calcularResultadoUnidad(
            1, u, res, new BigDecimal("5.00"), LIMITE_MAXIMO
        );
        
        // Base 80 + Bonus 5 = 85.
        assertEquals(new BigDecimal("85.00"), ru.getResultadoFinal());
    }

    // ── Pruebas de Calificación Final ────────────────────────────────────────

    @Test
    @DisplayName("Promedio de unidades correcto")
    void calcularPromedioUnidades_correcto() {
        List<ResultadoUnidad> unidades = Arrays.asList(
            unidadConFinal("80.00"), unidadConFinal("90.00")
        );
        assertEquals(new BigDecimal("85.00"), service.calcularPromedioUnidades(unidades));
    }

    @Test
    @DisplayName("Override tiene prioridad absoluta en la final")
    void calcularCalificacionFinal_overridePrioridad() throws Exception {
        Alumno a = new Alumno(); a.setId(1);
        List<ResultadoUnidad> unidades = Arrays.asList(unidadConFinal("70.00"));
        
        CalificacionFinal cf = service.calcularCalificacionFinal(
            1, a, unidades, null, new BigDecimal("100.00"), "Proyecto excelente", LIMITE_MAXIMO
        );
        
        assertTrue(cf.isEsOverride());
        assertEquals(new BigDecimal("100.00"), cf.getCalificacionFinal());
    }

    // ── Pruebas de Estados Académicos ────────────────────────────────────────

    @Test
    @DisplayName("Estado es APROBADO si alcanza o supera el mínimo del grupo")
    void determinarEstado_aprobado() {
        assertEquals("APROBADO", service.determinarEstado(new BigDecimal("70.00"), LIMITE_MINIMO));
        assertEquals("APROBADO", service.determinarEstado(new BigDecimal("85.00"), LIMITE_MINIMO));
    }

    @Test
    @DisplayName("Estado es REPROBADO si no alcanza el mínimo del grupo")
    void determinarEstado_reprobado() {
        assertEquals("REPROBADO", service.determinarEstado(new BigDecimal("69.99"), LIMITE_MINIMO));
    }

    @Test
    @DisplayName("Calificación null es PENDIENTE")
    void determinarEstado_pendiente() {
        assertEquals("PENDIENTE", service.determinarEstado(null, LIMITE_MINIMO));
    }

    // ── Helpers de construcción ──────────────────────────────────────────────

    private ActividadGrupo actividad(double ponderacion) {
        ActividadGrupo a = new ActividadGrupo();
        a.setPonderacion(BigDecimal.valueOf(ponderacion));
        return a;
    }

    private Resultado resultado(double calificacion, double ponderacion) {
        Resultado r = new Resultado();
        r.setCalificacion(BigDecimal.valueOf(calificacion));
        r.setPonderacion(BigDecimal.valueOf(ponderacion)); // El setter interno calcula la aportación
        return r;
    }

    private ResultadoUnidad unidadConFinal(String valor) {
        ResultadoUnidad ru = new ResultadoUnidad();
        ru.setResultadoFinal(new BigDecimal(valor));
        return ru;
    }
}