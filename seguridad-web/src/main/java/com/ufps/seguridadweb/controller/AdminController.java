package com.ufps.seguridadweb.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ufps.seguridadweb.service.UsuarioService;

/** Doble barrera: HttpSecurity (/admin/**) y @PreAuthorize a nivel de metodo. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/panel")
    @PreAuthorize("hasRole('ADMIN')")
    public String panel(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/panel";
    }
}
