package com.ufps.seguridadweb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ufps.seguridadweb.model.Comentario;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    @Query("SELECT c FROM Comentario c ORDER BY c.fecha DESC, c.id DESC")
    List<Comentario> listarRecientes();
}
