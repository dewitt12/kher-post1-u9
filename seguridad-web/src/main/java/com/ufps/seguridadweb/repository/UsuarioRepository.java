package com.ufps.seguridadweb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ufps.seguridadweb.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Consulta parametrizada: el valor viaja como parametro, nunca concatenado. */
    @Query("SELECT u FROM Usuario u WHERE u.username = :username")
    Optional<Usuario> buscarPorUsername(@Param("username") String username);

    /** Busqueda parcial, tambien parametrizada (el comodin se arma en JPQL, no en Java). */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Usuario> buscarPorTermino(@Param("termino") String termino);
}
