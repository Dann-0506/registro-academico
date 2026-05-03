package com.sira.controller;

import com.sira.service.RespaldoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/respaldos")
@PreAuthorize("hasRole('ADMIN')")
public class RespaldosController {

    @Autowired private RespaldoService respaldoService;

    @GetMapping("/descargar")
    public ResponseEntity<byte[]> descargar() {
        byte[] dump = respaldoService.generarDump();
        String nombre = "sira_respaldo_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(dump);
    }

    @PostMapping("/restaurar")
    public ResponseEntity<Void> restaurar(@RequestParam("archivo") MultipartFile archivo) {
        respaldoService.restaurarDump(archivo);
        return ResponseEntity.noContent().build();
    }
}
