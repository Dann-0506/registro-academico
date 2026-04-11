package com.academico.dao;

import com.academico.model.Configuracion;
import com.academico.util.DatabaseManagerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ConfiguracionDAO {

    public Optional<Configuracion> findByClave(String clave) throws SQLException {
        String sql = "SELECT * FROM configuracion WHERE clave = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Configuracion(
                        rs.getString("clave"),
                        rs.getString("valor"),
                        rs.getString("descripcion")
                    ));
                }
                return Optional.empty();
            }
        }
    }

    public void actualizarValor(String clave, String valor) throws SQLException {
        String sql = "UPDATE configuracion SET valor = ? WHERE clave = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, valor);
            ps.setString(2, clave);
            ps.executeUpdate();
        }
    }
}