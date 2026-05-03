package com.sira.repository;

import com.sira.model.ActividadGrupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ActividadGrupoRepository extends JpaRepository<ActividadGrupo, Integer> {

    @Query("""
        SELECT a FROM ActividadGrupo a
        JOIN FETCH a.grupo
        JOIN FETCH a.unidad
        LEFT JOIN FETCH a.actividadCatalogo
        WHERE a.grupo.id = :grupoId
        ORDER BY a.unidad.numero ASC, a.creadoEn ASC
        """)
    List<ActividadGrupo> findByGrupoIdWithUnidad(Integer grupoId);

    @Query("""
        SELECT a FROM ActividadGrupo a
        JOIN FETCH a.grupo
        JOIN FETCH a.unidad
        LEFT JOIN FETCH a.actividadCatalogo
        WHERE a.grupo.id = :grupoId AND a.unidad.id = :unidadId
        ORDER BY a.creadoEn ASC
        """)
    List<ActividadGrupo> findByGrupoIdAndUnidadId(Integer grupoId, Integer unidadId);

    @Query("SELECT COALESCE(SUM(a.ponderacion), 0) FROM ActividadGrupo a WHERE a.grupo.id = :grupoId AND a.unidad.id = :unidadId")
    BigDecimal sumPonderacionByGrupoIdAndUnidadId(Integer grupoId, Integer unidadId);

    long countByGrupoIdAndUnidadId(Integer grupoId, Integer unidadId);

    boolean existsByActividadCatalogoId(Integer actividadCatalogoId);
}
