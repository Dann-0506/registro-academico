package com.sira.repository;

import com.sira.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Integer> {

    @Query("SELECT m FROM Materia m LEFT JOIN FETCH m.unidades WHERE m.clave = :clave")
    Optional<Materia> findByClaveWithUnidades(String clave);

    Optional<Materia> findByClave(String clave);

    boolean existsByClave(String clave);

    @Query("SELECT DISTINCT m FROM Materia m LEFT JOIN FETCH m.unidades ORDER BY m.nombre ASC")
    List<Materia> findAllWithUnidades();

    @Query("SELECT DISTINCT m FROM Materia m LEFT JOIN FETCH m.unidades WHERE m.id = :id")
    Optional<Materia> findByIdWithUnidades(Integer id);
}
