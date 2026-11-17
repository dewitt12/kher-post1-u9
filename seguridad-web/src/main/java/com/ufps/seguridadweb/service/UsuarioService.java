package com.ufps.seguridadweb.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ufps.seguridadweb.model.Usuario;
import com.ufps.seguridadweb.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /** Usa la consulta parametrizada: una entrada como ' OR '1'='1 se trata como texto, no como SQL. */
    public List<Usuario> buscar(String termino) {
        return usuarioRepository.buscarPorTermino(termino);
    }

    public List<Usuario> buscarExacto(String username) {
        return usuarioRepository.buscarPorUsername(username).map(List::of).orElse(List.of());
    }
}
