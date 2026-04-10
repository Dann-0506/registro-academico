package com.academico.service.individuals;

import com.academico.dao.ResultadoDAO;
import com.academico.model.Resultado;
import com.academico.util.DatabaseManagerUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio encargado de la gestión de calificaciones (Resultados).
 * Centraliza la lectura y escritura, validando siempre el estado de la unidad.
 */
public class ResultadoService {

    private final ResultadoDAO resultadoDAO;
    private final EstadoUnidadService estadoUnidadService;

    public ResultadoService() {
        this.resultadoDAO = new ResultadoDAO();
        this.estadoUnidadService = new EstadoUnidadService();
    }

    public ResultadoService(ResultadoDAO resultadoDAO, EstadoUnidadService estadoUnidadService) {
        this.resultadoDAO = resultadoDAO;
        this.estadoUnidadService = estadoUnidadService;
    }

    /**
     * Busca las calificaciones de un alumno en una unidad específica.
     * Utilizado principalmente por ReporteService.
     */
    public List<Resultado> buscarPorInscripcionYUnidad(int inscripcionId, int unidadId) throws Exception {
        try {
            return resultadoDAO.findByInscripcionYUnidad(inscripcionId, unidadId);
        } catch (SQLException e) {
            throw new Exception("Error al consultar las calificaciones del estudiante.");
        }
    }

    /**
     * Guarda o actualiza una calificación individual.
     * Verifica que la unidad académica no esté bloqueada.
     */
    public void guardarCalificacion(int inscripcionId, int grupoId, int unidadId, int actividadId, BigDecimal nota) throws Exception {
        // Validación de reglas de negocio: Unidad Abierta
        estadoUnidadService.validarUnidadAbierta(grupoId, unidadId);

        try {
            resultadoDAO.guardar(inscripcionId, actividadId, nota);
        } catch (SQLException e) {
            throw new Exception("No se pudo registrar la calificación. Verifique la conexión.");
        }
    }

    /**
     * Guarda un lote de calificaciones (toda una unidad para el grupo).
     * Implementa un bloqueo preventivo (Locking) para asegurar la integridad.
     */
    public void guardarLote(int grupoId, int unidadId, List<Resultado> resultados) throws Exception {
        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Bloqueo de fila para evitar modificaciones simultáneas durante la carga
                String lockSql = "SELECT estado FROM estado_unidad WHERE grupo_id = ? AND unidad_id = ? FOR UPDATE";
                try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                    ps.setInt(1, grupoId);
                    ps.setInt(2, unidadId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && "CERRADA".equals(rs.getString("estado"))) {
                            throw new IllegalStateException("La unidad ha sido cerrada por otro proceso.");
                        }
                    }
                }

                // Persistencia masiva delegada al DAO
                resultadoDAO.guardarLoteEnConexion(conn, resultados);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new Exception("Error al procesar la carga masiva: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new Exception("Error de conexión al intentar guardar el lote.");
        }
    }
}