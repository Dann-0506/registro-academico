package com.sira.repository;

import com.sira.model.ActividadCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadCatalogoRepository extends JpaRepository<ActividadCatalogo, Integer> {

    boolean existsByNombre(String nombre);

    List<ActividadCatalogo> findAllByOrderByNombreAsc();

    List<ActividadCatalogo> findByActivoTrueOrderByNombreAsc();
}
