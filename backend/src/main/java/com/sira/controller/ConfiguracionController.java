package com.sira.controller;

import com.sira.dto.ConfiguracionLimitesRequest;
import com.sira.service.ConfiguracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/configuracion")
@PreAuthorize("hasRole('ADMIN')")
public class ConfiguracionController {

    @Autowired private ConfiguracionService configuracionService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtener() {
        return ResponseEntity.ok(Map.of(
                "minimaAprobatoria", configuracionService.obtenerCalificacionMinima(),
                "maxima", configuracionService.obtenerCalificacionMaxima(),
                "semestreActivo", configuracionService.obtenerSemestreActivo()
        ));
    }

    @PutMapping
    public ResponseEntity<Void> actualizar(@RequestBody ConfiguracionLimitesRequest request) {
        configuracionService.actualizar(request.minimaAprobatoria(), request.maxima(), request.semestreActivo());
        return ResponseEntity.noContent().build();
    }
}
