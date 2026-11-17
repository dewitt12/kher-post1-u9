package com.ufps.seguridadweb.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ufps.seguridadweb.model.Comentario;
import com.ufps.seguridadweb.repository.ComentarioRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository) {
        this.comentarioRepository = comentarioRepository;
    }

    public List<Comentario> listarRecientes() {
        return comentarioRepository.listarRecientes();
    }

    public Comentario guardar(String autor, String texto) {
        return comentarioRepository.save(new Comentario(autor, texto));
    }
}
