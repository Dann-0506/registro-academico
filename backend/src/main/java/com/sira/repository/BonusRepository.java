package com.sira.repository;

import com.sira.model.Bonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BonusRepository extends JpaRepository<Bonus, Integer> {

    List<Bonus> findByInscripcionIdOrderByOtorgadoEnAsc(Integer inscripcionId);

    Optional<Bonus> findByInscripcionIdAndUnidadIdAndTipo(Integer inscripcionId, Integer unidadId, String tipo);

    Optional<Bonus> findByInscripcionIdAndTipoAndUnidadIsNull(Integer inscripcionId, String tipo);

    List<Bonus> findByInscripcionIdAndTipo(Integer inscripcionId, String tipo);
}
