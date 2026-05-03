package com.sira.controller;

import com.sira.dto.CargaResultadoResponse;
import com.sira.service.CargaDatosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/carga")
@PreAuthorize("hasRole('ADMIN')")
public class CargaController {

    @Autowired private CargaDatosService cargaDatosService;

    @PostMapping("/csv")
    public ResponseEntity<CargaResultadoResponse> importar(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipo") String tipo) {

        CargaResultadoResponse resultado = switch (tipo.toLowerCase()) {
            case "alumnos"          -> cargaDatosService.importarAlumnos(archivo);
            case "maestros"         -> cargaDatosService.importarMaestros(archivo);
            case "administradores"  -> cargaDatosService.importarAdministradores(archivo);
            case "materias"         -> cargaDatosService.importarMaterias(archivo);
            case "grupos"           -> cargaDatosService.importarGrupos(archivo);
            case "inscripciones"    -> cargaDatosService.importarInscripciones(archivo);
            case "actividades"      -> cargaDatosService.importarActividades(archivo);
            default -> throw new IllegalArgumentException(
                    "Tipo desconocido: '" + tipo + "'. Válidos: alumnos, maestros, administradores, materias, grupos, inscripciones, actividades.");
        };

        return ResponseEntity.ok(resultado);
    }
}
