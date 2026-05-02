package com.sira.service;

import com.sira.dto.CalificacionFinalDto;
import com.sira.dto.ResultadoUnidadDto;
import com.sira.model.ActividadGrupo;
import com.sira.model.Resultado;
import com.sira.model.Unidad;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
public class CalificacionService {

    private static final int ESCALA = 2;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
    private static final BigDecimal PONDERACION_TOTAL = new BigDecimal("100.00");

    // ==========================================
    // VALIDACIÓN DE PONDERACIONES
    // ==========================================

    public BigDecimal sumarPonderaciones(List<ActividadGrupo> actividades) {
        if (actividades == null || actividades.isEmpty()) return BigDecimal.ZERO;
        return actividades.stream()
                .map(ActividadGrupo::getPonderacion)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ESCALA, REDONDEO);
    }

    public boolean ponderacionesValidas(List<ActividadGrupo> actividades) {
        return sumarPonderaciones(actividades).compareTo(PONDERACION_TOTAL) == 0;
    }

    public BigDecimal ponderacionFaltante(List<ActividadGrupo> actividades) {
        return PONDERACION_TOTAL.subtract(sumarPonderaciones(actividades)).setScale(ESCALA, REDONDEO);
    }

    // ==========================================
    // CÁLCULO POR UNIDAD
    // ==========================================

    public BigDecimal calcularResultadoBase(List<Resultado> resultados) {
        if (resultados == null || resultados.isEmpty()) return null;
        boolean hayAlguna = resultados.stream().anyMatch(r -> r.getCalificacion() != null);
        if (!hayAlguna) return null;
        return resultados.stream()
                .map(Resultado::getAportacion)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ESCALA, REDONDEO);
    }

    public BigDecimal aplicarBonusUnidad(BigDecimal base, BigDecimal bonusPuntos, BigDecimal limiteMaximo) {
        if (base == null) return null;
        if (bonusPuntos == null || bonusPuntos.compareTo(BigDecimal.ZERO) <= 0) return base;
        return base.add(bonusPuntos).min(limiteMaximo).setScale(ESCALA, REDONDEO);
    }

    public ResultadoUnidadDto calcularResultadoUnidad(
            Integer inscripcionId, Unidad unidad, List<Resultado> resultados,
            BigDecimal bonusPuntos, BigDecimal limiteMaximo) {

        BigDecimal base = calcularResultadoBase(resultados);
        BigDecimal bonus = bonusPuntos != null ? bonusPuntos : BigDecimal.ZERO;
        BigDecimal final_ = aplicarBonusUnidad(base, bonus, limiteMaximo);

        long calificadas = resultados == null ? 0 : resultados.stream()
                .filter(r -> r.getCalificacion() != null).count();

        ResultadoUnidadDto ru = new ResultadoUnidadDto();
        ru.setInscripcionId(inscripcionId);
        ru.setUnidadId(unidad.getId());
        ru.setUnidadNumero(unidad.getNumero());
        ru.setUnidadNombre(unidad.getNombre());
        ru.setResultadoBase(base);
        ru.setBonusPuntos(bonus);
        ru.setResultadoFinal(final_);
        ru.setActividadesCalificadas((int) calificadas);
        ru.setActividadesTotales(resultados == null ? 0 : resultados.size());
        return ru;
    }

    // ==========================================
    // CÁLCULO FINAL (Materia)
    // ==========================================

    public BigDecimal calcularPromedioUnidades(List<ResultadoUnidadDto> unidades) {
        if (unidades == null || unidades.isEmpty()) return null;
        List<BigDecimal> finales = unidades.stream()
                .map(ResultadoUnidadDto::getResultadoFinal)
                .filter(Objects::nonNull)
                .toList();
        if (finales.isEmpty()) return null;
        BigDecimal suma = finales.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(finales.size()), ESCALA, REDONDEO);
    }

    public BigDecimal aplicarBonusMateria(BigDecimal promedio, BigDecimal bonusPuntos, BigDecimal limiteMaximo) {
        if (promedio == null) return null;
        if (bonusPuntos == null || bonusPuntos.compareTo(BigDecimal.ZERO) <= 0) return promedio;
        return promedio.add(bonusPuntos).min(limiteMaximo).setScale(ESCALA, REDONDEO);
    }

    public CalificacionFinalDto calcularCalificacionFinal(
            Integer inscripcionId, Integer alumnoId, String alumnoNombre, String alumnoNumControl,
            List<ResultadoUnidadDto> unidades, BigDecimal bonusMateria,
            BigDecimal override, String overrideJustificacion, BigDecimal limiteMaximo) {

        BigDecimal calculada = calcularPromedioUnidades(unidades);
        BigDecimal bonus = bonusMateria != null ? bonusMateria : BigDecimal.ZERO;
        BigDecimal conBonus = aplicarBonusMateria(calculada, bonus, limiteMaximo);
        BigDecimal definitiva = override != null ? override : conBonus;

        CalificacionFinalDto cf = new CalificacionFinalDto();
        cf.setInscripcionId(inscripcionId);
        cf.setAlumnoId(alumnoId);
        cf.setAlumnoNombre(alumnoNombre);
        cf.setAlumnoNumControl(alumnoNumControl);
        cf.setUnidades(unidades);
        cf.setCalificacionCalculada(calculada);
        cf.setBonusMateria(bonus);
        cf.setCalificacionConBonus(conBonus);
        cf.setCalificacionFinal(definitiva);
        cf.setEsOverride(override != null);
        cf.setOverrideJustificacion(overrideJustificacion);
        return cf;
    }

    public String determinarEstado(BigDecimal calificacion, BigDecimal limiteMinimo) {
        if (calificacion == null) return "PENDIENTE";
        return calificacion.compareTo(limiteMinimo) >= 0 ? "APROBADO" : "REPROBADO";
    }
}
