package com.sira.repository;

import com.sira.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    @Query("""
        SELECT i FROM Inscripcion i
        JOIN FETCH i.alumno a
        JOIN FETCH a.usuario
        JOIN FETCH i.grupo g
        JOIN FETCH g.materia
        WHERE i.id = :id
        """)
    Optional<Inscripcion> findByIdWithDetails(Integer id);

    Optional<Inscripcion> findByAlumnoIdAndGrupoId(Integer alumnoId, Integer grupoId);

    @Query("""
        SELECT i FROM Inscripcion i
        JOIN FETCH i.alumno a
        JOIN FETCH a.usuario
        JOIN FETCH i.grupo g
        JOIN FETCH g.materia
        WHERE i.grupo.id = :grupoId
        ORDER BY a.usuario.nombre ASC
        """)
    List<Inscripcion> findByGrupoIdWithAlumno(Integer grupoId);

    @Query("""
        SELECT i FROM Inscripcion i
        JOIN FETCH i.alumno a
        JOIN FETCH a.usuario
        JOIN FETCH i.grupo g
        JOIN FETCH g.materia
        WHERE i.alumno.id = :alumnoId
        ORDER BY g.semestre DESC
        """)
    List<Inscripcion> findByAlumnoIdWithGrupo(Integer alumnoId);

    @Query("""
        SELECT i FROM Inscripcion i
        JOIN FETCH i.alumno a
        JOIN FETCH a.usuario
        JOIN FETCH i.grupo g
        JOIN FETCH g.materia
        ORDER BY a.usuario.nombre ASC
        """)
    List<Inscripcion> findAllWithDetails();

    long countByGrupoId(Integer grupoId);

    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.grupo.estadoEvaluacion = 'ABIERTO' AND i.grupo.activo = true AND i.grupo.semestre = :semestre")
    long countInscripcionesActivasPorSemestre(String semestre);

    // ─── Queries para reportes estratégicos ───────────────────────────────────

    @Query(value = """
        SELECT m.id        AS materia_id,
               m.clave     AS clave,
               m.nombre    AS nombre,
               COUNT(DISTINCT g.id)                                         AS grupos_evaluados,
               COUNT(i.id)                                                  AS total_alumnos,
               SUM(CASE WHEN i.estado_academico='APROBADO'  THEN 1 ELSE 0 END) AS aprobados,
               SUM(CASE WHEN i.estado_academico='REPROBADO' THEN 1 ELSE 0 END) AS reprobados
        FROM inscripcion i
        JOIN grupo   g ON i.grupo_id   = g.id
        JOIN materia m ON g.materia_id = m.id
        WHERE g.estado_evaluacion = 'CERRADO'
          AND g.semestre = :semestre
          AND i.estado_academico IN ('APROBADO','REPROBADO')
        GROUP BY m.id, m.clave, m.nombre
        ORDER BY reprobados DESC
        """, nativeQuery = true)
    List<Object[]> findMateriasConReprobacionRaw(String semestre);

    @Query(value = """
        SELECT ma.id          AS maestro_id,
               u.nombre       AS nombre,
               ma.num_empleado AS num_empleado,
               COUNT(DISTINCT g.id)                                          AS grupos,
               COUNT(i.id)                                                   AS alumnos_evaluados,
               SUM(CASE WHEN i.estado_academico='APROBADO'  THEN 1 ELSE 0 END) AS aprobados,
               SUM(CASE WHEN i.estado_academico='REPROBADO' THEN 1 ELSE 0 END) AS reprobados
        FROM inscripcion i
        JOIN grupo   g  ON i.grupo_id    = g.id
        JOIN maestro ma ON g.maestro_id  = ma.id
        JOIN usuario u  ON ma.usuario_id = u.id
        WHERE g.estado_evaluacion = 'CERRADO'
          AND g.semestre = :semestre
          AND i.estado_academico IN ('APROBADO','REPROBADO')
        GROUP BY ma.id, u.nombre, ma.num_empleado
        ORDER BY u.nombre ASC
        """, nativeQuery = true)
    List<Object[]> findMaestrosAprovechamientoRaw(String semestre);

    @Query("""
        SELECT i FROM Inscripcion i
        JOIN FETCH i.alumno a JOIN FETCH a.usuario
        JOIN FETCH i.grupo g JOIN FETCH g.materia
        WHERE i.estadoAcademico = 'REPROBADO'
        AND g.semestre = :semestre
        ORDER BY a.usuario.nombre ASC
        """)
    List<Inscripcion> findReprobadosPorSemestre(String semestre);

    boolean existsByAlumnoIdAndGrupoId(Integer alumnoId, Integer grupoId);

    boolean existsByAlumnoId(Integer alumnoId);

    boolean existsByGrupoId(Integer grupoId);

    @Modifying
    @Query("""
        UPDATE Inscripcion i SET
            i.calificacionFinalCalculada = :calificacion,
            i.estadoAcademico = :estado
        WHERE i.id = :id
        """)
    void guardarResultadoHistorico(Integer id, BigDecimal calificacion, String estado);

    @Modifying
    @Query("""
        UPDATE Inscripcion i SET
            i.calificacionFinalOverride = :override,
            i.overrideJustificacion = :justificacion,
            i.estadoAcademico = :estado
        WHERE i.id = :id
        """)
    void actualizarOverride(Integer id, BigDecimal override, String justificacion, String estado);
}
