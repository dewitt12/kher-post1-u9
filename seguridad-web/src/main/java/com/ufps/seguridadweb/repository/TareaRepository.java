package com.ufps.seguridadweb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ufps.seguridadweb.model.Tarea;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    @Query("SELECT t FROM Tarea t WHERE t.propietario = :propietario ORDER BY t.id")
    List<Tarea> listarPorPropietario(@Param("propietario") String propietario);
}
