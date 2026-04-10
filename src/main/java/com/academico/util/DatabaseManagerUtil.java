package com.academico.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class DatabaseManagerUtil {
    private static HikariDataSource dataSource;

    public static void initialize() {
        Dotenv dotenv = Dotenv.load();
        HikariConfig config = new HikariConfig();
        
        config.setJdbcUrl("jdbc:postgresql://" + dotenv.get("DB_HOST") + ":" + 
                          dotenv.get("DB_PORT") + "/" + dotenv.get("DB_NAME"));
        config.setUsername(dotenv.get("DB_USER"));
        config.setPassword(dotenv.get("DB_PASSWORD"));
        
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);
        
        createSchemaIfNeeded();
    }

    private static void createSchemaIfNeeded() {
        // OPTIMIZACIÓN: Solo ejecutamos el script si la tabla clave no existe
        try (Connection conn = getConnection()) {
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "configuracion", null);
            
            if (!tables.next()) {
                System.out.println("Base de datos nueva detectada. Instalando esquema...");
                ejecutarScript(conn);
            } else {
                System.out.println("Base de datos lista.");
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar esquema: " + e.getMessage());
        }
    }

    private static void ejecutarScript(Connection conn) {
        try (InputStream is = DatabaseManagerUtil.class.getResourceAsStream("/com/academico/db/schema.sql")) {
            if (is == null) return;
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public static boolean isInitialized() {
        return dataSource != null && !dataSource.isClosed();
    }
}