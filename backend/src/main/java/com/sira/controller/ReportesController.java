package com.sira.controller;

import com.sira.dto.ReportesResponse;
import com.sira.service.ConfiguracionService;
import com.sira.service.ReportesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReportesController {

    @Autowired private ReportesService reportesService;
    @Autowired private ConfiguracionService configuracionService;

    @GetMapping("/semestres")
    public List<String> getSemestres() {
        return reportesService.getSemestresDisponibles();
    }

    @GetMapping
    public ReportesResponse getReportes(@RequestParam(required = false) String semestre) {
        String sem = (semestre != null && !semestre.isBlank())
                ? semestre
                : configuracionService.obtenerSemestreActivo();
        return reportesService.generarReportes(sem);
    }
}
