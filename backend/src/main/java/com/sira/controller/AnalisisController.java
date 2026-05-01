package com.sira.controller;

import com.sira.dto.KpiResponse;
import com.sira.dto.RendimientoResponse;
import com.sira.service.AnalisisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analisis")
@PreAuthorize("hasRole('ADMIN')")
public class AnalisisController {

    @Autowired private AnalisisService analisisService;

    @GetMapping("/kpis")
    public KpiResponse kpis() {
        return analisisService.obtenerKpis();
    }

    @GetMapping("/rendimiento")
    public List<RendimientoResponse> rendimiento() {
        return analisisService.obtenerRendimientoPorSemestre();
    }
}
