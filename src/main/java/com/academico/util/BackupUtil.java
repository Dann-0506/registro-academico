package com.academico.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;

public class BackupUtil {

    public static boolean crearRespaldoAuto(String rutaDestino) {
        Dotenv dotenv = Dotenv.load();
        return crearRespaldo(
            dotenv.get("DB_HOST"),
            dotenv.get("DB_PORT"),
            dotenv.get("DB_USER"),
            dotenv.get("DB_PASSWORD"),
            dotenv.get("DB_NAME"),
            rutaDestino
        );
    }

    public static boolean crearRespaldo(String host, String puerto, String usuario, 
                                        String password, String nombreBd, String rutaDestino) {
        try {
            // VERIFICACIÓN DE HERRAMIENTA (Prevención de errores)
            if (!comandoExiste("pg_dump")) {
                System.err.println("Error: 'pg_dump' no está instalado o no está en el PATH.");
                return false;
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump", "-h", host, "-p", puerto, "-U", usuario,
                    "-F", "c", "-f", rutaDestino, nombreBd
            );

            pb.environment().put("PGPASSWORD", password);
            Process proceso = pb.start();
            return proceso.waitFor() == 0;
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error en respaldo: " + e.getMessage());
            return false;
        }
    }

    private static boolean comandoExiste(String comando) {
        try {
            Process p = new ProcessBuilder(comando, "--version").start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}