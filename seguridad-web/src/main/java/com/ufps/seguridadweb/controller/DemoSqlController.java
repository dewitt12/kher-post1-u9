package com.ufps.seguridadweb.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufps.seguridadweb.model.Usuario;
import com.ufps.seguridadweb.service.UsuarioService;

/** Version SEGURA: usa @Query parametrizado. Una entrada tipo ' OR '1'='1 no devuelve nada. */
@RestController
public class DemoSqlController {

    private final UsuarioService usuarioService;

    public DemoSqlController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/demo/sqli-seguro")
    public List<String> buscarSeguro(@RequestParam String nombre) {
        return usuarioService.buscarExacto(nombre).stream().map(Usuario::getUsername).toList();
    }
}
