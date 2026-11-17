package com.ufps.seguridadweb.controller;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ufps.seguridadweb.repository.ConsultaVulnerableDao;
import com.ufps.seguridadweb.service.ComentarioService;

/**
 * PRUEBA TEMPORAL del laboratorio (solo con app.demo.vulnerable=true).
 * Muestra el contraste: th:utext ejecuta el script, SQL concatenado es inyectable.
 */
@Controller
@ConditionalOnProperty(name = "app.demo.vulnerable", havingValue = "true")
public class DemoInseguroController {

    private final ComentarioService comentarioService;
    private final ConsultaVulnerableDao consultaVulnerableDao;

    public DemoInseguroController(ComentarioService comentarioService,
                                  ConsultaVulnerableDao consultaVulnerableDao) {
        this.comentarioService = comentarioService;
        this.consultaVulnerableDao = consultaVulnerableDao;
    }

    @GetMapping("/demo/xss-utext")
    public String xssUtext(Model model) {
        model.addAttribute("comentarios", comentarioService.listarRecientes());
        return "demo/xss-utext";
    }

    @GetMapping("/demo/sqli-inseguro")
    @ResponseBody
    public List<String> sqliInseguro(@RequestParam String nombre) {
        return consultaVulnerableDao.buscarUsernameInseguro(nombre);
    }
}
