package com.sira.service;

import com.sira.dto.*;
import com.sira.model.Inscripcion;
import com.sira.repository.GrupoRepository;
import com.sira.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportesService {

    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private GrupoRepository grupoRepository;

    @Transactional(readOnly = true)
    public List<String> getSemestresDisponibles() {
        return grupoRepository.findSemestresConActaCerrada();
    }

    @Transactional(readOnly = true)
    public ReportesResponse generarReportes(String semestre) {
        return new ReportesResponse(
                semestre,
                buildMateriasReprobacion(semestre),
                buildAlumnosRiesgo(semestre),
                buildMaestrosAprovechamiento(semestre)
        );
    }

    // ─── Materias con mayor reprobación ──────────────────────────────────────

    private List<MateriaReprobacionDto> buildMateriasReprobacion(String semestre) {
        return inscripcionRepository.findMateriasConReprobacionRaw(semestre)
                .stream()
                .map(row -> {
                    long total = toLong(row[4]);
                    long reprobados = toLong(row[6]);
                    double pct = total > 0 ? Math.round((reprobados * 100.0 / total) * 10.0) / 10.0 : 0;
                    return new MateriaReprobacionDto(
                            toInt(row[0]), str(row[1]), str(row[2]),
                            toLong(row[3]), total, toLong(row[5]), reprobados, pct);
                })
                .toList();
    }

    // ─── Alumnos en riesgo académico ──────────────────────────────────────────

    private List<AlumnoRiesgoDto> buildAlumnosRiesgo(String semestre) {
        List<Inscripcion> reprobadas = inscripcionRepository.findReprobadosPorSemestre(semestre);

        Map<Integer, List<Inscripcion>> porAlumno = reprobadas.stream()
                .collect(Collectors.groupingBy(i -> i.getAlumno().getId()));

        return porAlumno.entrySet().stream()
                .filter(e -> e.getValue().size() >= 2)
                .map(e -> {
                    List<Inscripcion> ins = e.getValue();
                    Inscripcion primera = ins.get(0);
                    List<String> grupos = ins.stream()
                            .map(i -> i.getGrupo().getMateria().getNombre() + " (" + i.getGrupo().getClave() + ")")
                            .toList();
                    return new AlumnoRiesgoDto(
                            primera.getAlumno().getId(),
                            primera.getAlumno().getMatricula(),
                            primera.getAlumno().getUsuario().getNombre(),
                            primera.getAlumno().getUsuario().getEmail(),
                            ins.size(),
                            grupos
                    );
                })
                .sorted(Comparator.comparingInt(AlumnoRiesgoDto::materiasReprobadas).reversed())
                .toList();
    }

    // ─── Índice de aprovechamiento por maestro ────────────────────────────────

    private List<MaestroAprovechamientoDto> buildMaestrosAprovechamiento(String semestre) {
        return inscripcionRepository.findMaestrosAprovechamientoRaw(semestre)
                .stream()
                .map(row -> {
                    long alumnosEval = toLong(row[4]);
                    long aprobados = toLong(row[5]);
                    double pct = alumnosEval > 0 ? Math.round((aprobados * 100.0 / alumnosEval) * 10.0) / 10.0 : 0;
                    return new MaestroAprovechamientoDto(
                            toInt(row[0]), str(row[1]), str(row[2]),
                            toLong(row[3]), alumnosEval, aprobados, toLong(row[6]), pct);
                })
                .toList();
    }

    // ─── Helpers de mapeo ────────────────────────────────────────────────────

    private static Integer toInt(Object o) {
        if (o == null) return null;
        return ((Number) o).intValue();
    }

    private static long toLong(Object o) {
        if (o == null) return 0L;
        return ((Number) o).longValue();
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
