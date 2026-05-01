package com.sira.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;

@Service
public class RespaldoService {

    @Value("${spring.datasource.username}") private String dbUser;
    @Value("${spring.datasource.password}") private String dbPassword;
    @Value("${db.host:localhost}")           private String dbHost;
    @Value("${db.port:5432}")               private String dbPort;
    @Value("${db.name:sira}")               private String dbName;

    public byte[] generarDump() {
        validarComando("pg_dump");
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump", "-h", dbHost, "-p", dbPort, "-U", dbUser,
                    "-F", "p", "--no-password", dbName
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(false);

            Process proceso = pb.start();
            byte[] dump = proceso.getInputStream().readAllBytes();
            int exitCode = proceso.waitFor();

            if (exitCode != 0) {
                String stderr = new String(proceso.getErrorStream().readAllBytes());
                throw new RuntimeException("pg_dump falló (código " + exitCode + "): " + stderr);
            }
            return dump;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error al ejecutar pg_dump: " + e.getMessage(), e);
        }
    }

    public void restaurarDump(MultipartFile archivo) {
        validarComando("psql");
        File temp = null;
        try {
            temp = Files.createTempFile("sira_restore_", ".sql").toFile();
            archivo.transferTo(temp);

            ProcessBuilder pb = new ProcessBuilder(
                    "psql", "-h", dbHost, "-p", dbPort, "-U", dbUser,
                    "-d", dbName, "-f", temp.getAbsolutePath()
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process proceso = pb.start();
            int exitCode = proceso.waitFor();

            if (exitCode != 0) {
                String stderr = new String(proceso.getInputStream().readAllBytes());
                throw new RuntimeException("psql falló (código " + exitCode + "): " + stderr);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error al restaurar el respaldo: " + e.getMessage(), e);
        } finally {
            if (temp != null) temp.delete();
        }
    }

    private void validarComando(String comando) {
        try {
            Process p = new ProcessBuilder(comando, "--version").start();
            if (p.waitFor() != 0) throw new RuntimeException();
        } catch (Exception e) {
            throw new IllegalStateException("'" + comando + "' no está disponible en el servidor.");
        }
    }
}
