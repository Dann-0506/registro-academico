package com.sira.repository;

import com.sira.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Integer> {

    @Query("""
        SELECT g FROM Grupo g
        JOIN FETCH g.materia
        JOIN FETCH g.maestro m
        JOIN FETCH m.usuario
        WHERE g.clave = :clave AND g.semestre = :semestre
        """)
    Optional<Grupo> findByClaveAndSemestre(String clave, String semestre);

    @Query("""
        SELECT g FROM Grupo g
        JOIN FETCH g.materia
        JOIN FETCH g.maestro m
        JOIN FETCH m.usuario
        ORDER BY g.semestre DESC, g.materia.nombre ASC
        """)
    List<Grupo> findAllWithDetails();

    @Query("""
        SELECT g FROM Grupo g
        JOIN FETCH g.materia
        JOIN FETCH g.maestro m
        JOIN FETCH m.usuario
        WHERE m.id = :maestroId AND g.estadoEvaluacion = 'ABIERTO'
        ORDER BY g.semestre DESC, g.materia.nombre ASC
        """)
    List<Grupo> findByMaestroIdAbiertos(Integer maestroId);

    @Query("""
        SELECT g FROM Grupo g
        JOIN FETCH g.materia
        JOIN FETCH g.maestro m
        JOIN FETCH m.usuario
        JOIN Inscripcion i ON i.grupo = g
        WHERE i.alumno.id = :alumnoId
        ORDER BY g.semestre DESC, g.materia.nombre ASC
        """)
    List<Grupo> findByAlumnoId(Integer alumnoId);

    @Query("""
        SELECT g FROM Grupo g
        JOIN FETCH g.materia
        JOIN FETCH g.maestro m
        JOIN FETCH m.usuario
        WHERE g.id = :id
        """)
    Optional<Grupo> findByIdWithDetails(Integer id);

    boolean existsByClaveAndMateriaIdAndSemestre(String clave, Integer materiaId, String semestre);

    boolean existsByMaestroId(Integer maestroId);
}
