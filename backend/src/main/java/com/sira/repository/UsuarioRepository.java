package com.sira.repository;

import com.sira.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRolOrderByNombreAsc(String rol);

    long countByRolAndActivo(String rol, boolean activo);

    @Modifying
    @Query("UPDATE Usuario u SET u.passwordHash = :hash WHERE u.id = :id")
    void actualizarPassword(Integer id, String hash);

    @Modifying
    @Query("UPDATE Usuario u SET u.activo = :activo WHERE u.id = :id")
    void actualizarActivo(Integer id, boolean activo);
}
