package com.sira.repository;

import com.sira.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Integer> {

    List<Unidad> findByMateriaIdOrderByNumeroAsc(Integer materiaId);

    boolean existsByMateriaIdAndNumero(Integer materiaId, int numero);
}
