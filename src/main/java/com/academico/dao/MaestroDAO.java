package com.academico.dao;

import com.academico.model.Maestro;
import com.academico.util.DatabaseManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaestroDAO {

    // === Mapeo de ResultSet a Maestro ===

    private Maestro mapear(ResultSet rs) throws SQLException {
        Maestro m = new Maestro();
        m.setId(rs.getInt("id"));
        m.setUsuarioId(rs.getInt("usuario_id"));
        m.setNumEmpleado(rs.getString("num_empleado"));
        m.setNombre(rs.getString("nombre"));
        m.setEmail(rs.getString("email"));
        return m;
    }


    // === Consultas ===

    public Optional<Maestro> findById(int id) throws SQLException {
        String sql = """
                SELECT m.*, u.nombre, u.email
                FROM maestro m
                JOIN usuario u ON u.id = m.usuario_id
                WHERE m.id = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Maestro> findByUsuarioId(int usuarioId) throws SQLException {
        String sql = """
                SELECT m.*, u.nombre, u.email
                FROM maestro m
                JOIN usuario u ON u.id = m.usuario_id
                WHERE m.usuario_id = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Maestro> findAll() throws SQLException {
        String sql = """
                SELECT m.*, u.nombre, u.email
                FROM maestro m
                JOIN usuario u ON u.id = m.usuario_id
                ORDER BY u.nombre
                """;
        List<Maestro> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }


    // === Escritura ===

    public Maestro insertar(Maestro m) throws SQLException {
        String sql = """
                INSERT INTO maestro (usuario_id, num_empleado)
                VALUES (?, ?)
                RETURNING id
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getUsuarioId());
            if (m.getNumEmpleado() != null) ps.setString(2, m.getNumEmpleado());
            else ps.setNull(2, Types.VARCHAR);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                m.setId(rs.getInt("id"));
            }
        }
        return m;
    }

    public void crear(Maestro m) throws SQLException {
        String sqlUsuario = """
                INSERT INTO usuario (nombre, email, password_hash, rol, activo)
                VALUES (?, ?, ?, 'maestro', true) RETURNING id
                """;
        String sqlMaestro = """
                INSERT INTO maestro (usuario_id, num_empleado)
                VALUES (?, ?)
                """;

        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false); // Inicio de transacción
            try {
                int nuevoUsuarioId = -1;

                // 1. Insertar en la tabla Usuario
                try (PreparedStatement psU = conn.prepareStatement(sqlUsuario)) {
                    psU.setString(1, m.getNombre());
                    psU.setString(2, m.getEmail());
                    // Hash de "123456"
                    psU.setString(3, "$2a$10$wE0vA1O3HhXyI2BqD2K1uuA5Q.h5N6q9g/zQZ/oQYy2C1K1c0kO6i"); 
                    try (ResultSet rs = psU.executeQuery()) {
                        if (rs.next()) nuevoUsuarioId = rs.getInt(1);
                    }
                }

                // 2. Insertar en la tabla Maestro
                try (PreparedStatement psM = conn.prepareStatement(sqlMaestro)) {
                    psM.setInt(1, nuevoUsuarioId);
                    psM.setString(2, m.getNumEmpleado());
                    psM.executeUpdate();
                }

                conn.commit(); // Éxito total
            } catch (SQLException e) {
                conn.rollback(); // Error: deshacemos todo
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}