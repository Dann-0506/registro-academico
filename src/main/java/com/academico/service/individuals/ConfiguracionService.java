package com.academico.service.individuals;

import com.academico.dao.ConfiguracionDAO;

import java.math.BigDecimal;
import java.sql.SQLException;

public class ConfiguracionService {

    private final ConfiguracionDAO configuracionDAO = new ConfiguracionDAO();

    public BigDecimal obtenerCalificacionMinima() throws Exception {
        return obtenerValorNumerico("calificacion_minima_aprobatoria", new BigDecimal("70"));
    }

    public BigDecimal obtenerCalificacionMaxima() throws Exception {
        return obtenerValorNumerico("calificacion_maxima", new BigDecimal("100"));
    }

    private BigDecimal obtenerValorNumerico(String clave, BigDecimal valorPorDefecto) throws Exception {
        try {
            return configuracionDAO.findByClave(clave)
                    .map(config -> new BigDecimal(config.getValor()))
                    .orElse(valorPorDefecto);
        } catch (SQLException | NumberFormatException e) {
            throw new Exception("Error al leer la configuración de la base de datos.");
        }
    }

    public void actualizarLimites(BigDecimal minima, BigDecimal maxima) throws Exception {
        if (minima == null || maxima == null) {
            throw new IllegalArgumentException("Ambas calificaciones son obligatorias.");
        }
        if (minima.compareTo(BigDecimal.ZERO) < 0 || maxima.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Las calificaciones deben ser números positivos.");
        }
        if (minima.compareTo(maxima) >= 0) {
            throw new IllegalArgumentException("La calificación mínima aprobatoria no puede ser mayor o igual a la máxima.");
        }

        try {
            configuracionDAO.actualizarValor("calificacion_minima_aprobatoria", minima.toString());
            configuracionDAO.actualizarValor("calificacion_maxima", maxima.toString());
        } catch (SQLException e) {
            throw new Exception("Error de conexión al guardar la configuración.");
        }
    }
}