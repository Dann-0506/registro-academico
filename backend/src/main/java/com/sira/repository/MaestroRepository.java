package com.sira.repository;

import com.sira.model.Maestro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaestroRepository extends JpaRepository<Maestro, Integer> {

    @Query("SELECT m FROM Maestro m JOIN FETCH m.usuario WHERE m.id = :id")
    Optional<Maestro> findByIdWithUsuario(Integer id);

    @Query("SELECT m FROM Maestro m JOIN FETCH m.usuario WHERE m.numEmpleado = :numEmpleado")
    Optional<Maestro> findByNumEmpleadoWithUsuario(String numEmpleado);

    @Query("SELECT m FROM Maestro m JOIN FETCH m.usuario WHERE m.usuario.id = :usuarioId")
    Optional<Maestro> findByUsuarioIdWithUsuario(Integer usuarioId);

    Optional<Maestro> findByNumEmpleado(String numEmpleado);

    Optional<Maestro> findByUsuarioId(Integer usuarioId);

    boolean existsByNumEmpleado(String numEmpleado);

    @Query("SELECT m FROM Maestro m JOIN FETCH m.usuario ORDER BY m.usuario.nombre ASC")
    List<Maestro> findAllWithUsuario();

    @Query("SELECT m FROM Maestro m JOIN FETCH m.usuario WHERE m.usuario.activo = true ORDER BY m.usuario.nombre ASC")
    List<Maestro> findAllActivosWithUsuario();
}
