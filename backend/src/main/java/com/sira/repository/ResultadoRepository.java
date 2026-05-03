package com.sira.repository;

import com.sira.model.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResultadoRepository extends JpaRepository<Resultado, Integer> {

    @Query("""
        SELECT r FROM Resultado r
        JOIN FETCH r.actividadGrupo ag
        JOIN FETCH ag.unidad
        WHERE r.inscripcion.id = :inscripcionId AND ag.unidad.id = :unidadId
        ORDER BY ag.creadoEn ASC
        """)
    List<Resultado> findByInscripcionIdAndUnidadId(Integer inscripcionId, Integer unidadId);

    @Query("""
        SELECT r FROM Resultado r
        JOIN FETCH r.actividadGrupo ag
        JOIN FETCH ag.unidad
        WHERE r.inscripcion.id = :inscripcionId
        ORDER BY ag.unidad.numero ASC, ag.creadoEn ASC
        """)
    List<Resultado> findByInscripcionId(Integer inscripcionId);

    Optional<Resultado> findByInscripcionIdAndActividadGrupoId(Integer inscripcionId, Integer actividadGrupoId);

    boolean existsByInscripcionIdAndActividadGrupoIdAndCalificacionIsNotNull(
        Integer inscripcionId, Integer actividadGrupoId);

    @Query("SELECT COUNT(r) > 0 FROM Resultado r WHERE r.actividadGrupo.id = :actividadId AND r.calificacion IS NOT NULL")
    boolean tieneCalificacionesRegistradas(Integer actividadId);

    @Query("""
        SELECT COUNT(i) FROM Inscripcion i
        WHERE i.grupo.id = :grupoId
        AND (
            SELECT COUNT(r) FROM Resultado r
            WHERE r.inscripcion.id = i.id
            AND r.actividadGrupo.unidad.id = :unidadId
            AND r.calificacion IS NOT NULL
        ) < (
            SELECT COUNT(a) FROM ActividadGrupo a
            WHERE a.grupo.id = :grupoId
            AND a.unidad.id = :unidadId
        )
        """)
    long countAlumnosSinCalificarEnUnidad(Integer grupoId, Integer unidadId);

    @Modifying
    @Query(value = """
        INSERT INTO resultado (inscripcion_id, actividad_grupo_id, calificacion, modificado_en)
        VALUES (:inscripcionId, :actividadGrupoId, :calificacion, NOW())
        ON CONFLICT (inscripcion_id, actividad_grupo_id)
        DO UPDATE SET calificacion = EXCLUDED.calificacion, modificado_en = NOW()
        """, nativeQuery = true)
    void upsert(Integer inscripcionId, Integer actividadGrupoId, BigDecimal calificacion);
}
