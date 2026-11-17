package com.ufps.seguridadweb.repository;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * SOLO PARA LA DEMOSTRACION DEL LABORATORIO.
 * Concatena la entrada del usuario dentro del SQL (vulnerable a SQL Injection).
 * Esta clase solo existe cuando app.demo.vulnerable=true; por defecto no se carga.
 */
@Repository
@ConditionalOnProperty(name = "app.demo.vulnerable", havingValue = "true")
public class ConsultaVulnerableDao {

    private final JdbcTemplate jdbcTemplate;

    public ConsultaVulnerableDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> buscarUsernameInseguro(String nombre) {
        // INSEGURO a proposito: concatenacion directa de la entrada.
        String sql = "SELECT username FROM usuarios WHERE username = '" + nombre + "'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}
