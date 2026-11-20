package com.ufps.seguridadweb.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ufps.seguridadweb.model.Tarea;
import com.ufps.seguridadweb.model.TareaRequest;
import com.ufps.seguridadweb.service.TareaService;

import jakarta.validation.Valid;

/** Endpoints protegidos por JWT (apiFilterChain). */
@RestController
@RequestMapping("/api/tareas")
public class TareaRestController {

    private final TareaService tareaService;

    public TareaRestController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    /** Tareas del usuario autenticado por el token. */
    @GetMapping
    public List<Tarea> mias(Authentication authentication) {
        return tareaService.listarDe(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tarea crear(@Valid @RequestBody TareaRequest peticion, Authentication authentication) {
        return tareaService.crear(peticion.titulo(), authentication.getName());
    }

    /** Solo ADMIN: con un token de USER responde 403. */
    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Tarea> todas() {
        return tareaService.listarTodas();
    }
}
