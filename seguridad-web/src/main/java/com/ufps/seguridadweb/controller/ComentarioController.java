package com.ufps.seguridadweb.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ufps.seguridadweb.service.ComentarioService;

@Controller
public class ComentarioController {

    private static final int MAX_LONGITUD = 500;

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping("/comentarios")
    public String listar(Model model) {
        model.addAttribute("comentarios", comentarioService.listarRecientes());
        return "comentarios";
    }

    /**
     * POST protegido por CSRF: sin el token del formulario, Spring Security responde 403.
     * El texto se guarda tal cual; la defensa contra XSS ocurre al mostrarlo con th:text (escapado).
     */
    @PostMapping("/comentarios")
    public String crear(@RequestParam String texto, Authentication authentication) {
        String limpio = texto == null ? "" : texto.strip();
        if (!limpio.isEmpty() && limpio.length() <= MAX_LONGITUD) {
            comentarioService.guardar(authentication.getName(), limpio);
        }
        return "redirect:/comentarios";
    }
}
