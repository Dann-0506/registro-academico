package com.sira.repository;

import com.sira.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

    @Query("SELECT a FROM Alumno a JOIN FETCH a.usuario WHERE a.id = :id")
    Optional<Alumno> findByIdWithUsuario(Integer id);

    @Query("SELECT a FROM Alumno a JOIN FETCH a.usuario WHERE a.matricula = :matricula")
    Optional<Alumno> findByMatriculaWithUsuario(String matricula);

    @Query("SELECT a FROM Alumno a JOIN FETCH a.usuario WHERE a.usuario.id = :usuarioId")
    Optional<Alumno> findByUsuarioIdWithUsuario(Integer usuarioId);

    Optional<Alumno> findByMatricula(String matricula);

    Optional<Alumno> findByUsuarioId(Integer usuarioId);

    boolean existsByMatricula(String matricula);

    @Query("SELECT a FROM Alumno a JOIN FETCH a.usuario ORDER BY a.usuario.nombre ASC")
    List<Alumno> findAllWithUsuario();

    @Query("""
        SELECT a FROM Alumno a JOIN FETCH a.usuario
        JOIN Inscripcion i ON i.alumno = a
        WHERE i.grupo.id = :grupoId
        ORDER BY a.usuario.nombre ASC
        """)
    List<Alumno> findByGrupoId(Integer grupoId);

    @Query("""
        SELECT a FROM Alumno a JOIN FETCH a.usuario
        WHERE a.usuario.activo = true
        AND NOT EXISTS (
            SELECT i FROM Inscripcion i
            WHERE i.alumno = a
            AND i.grupo.semestre = :semestre
            AND i.grupo.estadoEvaluacion = 'ABIERTO'
            AND i.grupo.activo = true
        )
        ORDER BY a.usuario.nombre ASC
        """)
    List<Alumno> findAlumnosSinInscripcionesEnSemestre(String semestre);
}
