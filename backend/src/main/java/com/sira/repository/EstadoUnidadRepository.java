package com.sira.repository;

import com.sira.model.EstadoUnidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoUnidadRepository extends JpaRepository<EstadoUnidad, Integer> {

    Optional<EstadoUnidad> findByGrupoIdAndUnidadId(Integer grupoId, Integer unidadId);

    List<EstadoUnidad> findByGrupoId(Integer grupoId);

    @Modifying
    @Query("UPDATE EstadoUnidad e SET e.estado = :estado WHERE e.grupo.id = :grupoId AND e.unidad.id = :unidadId")
    void actualizarEstado(Integer grupoId, Integer unidadId, String estado);
}
