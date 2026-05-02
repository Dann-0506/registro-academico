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
