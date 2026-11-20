package com.ufps.seguridadweb.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ufps.seguridadweb.model.Tarea;
import com.ufps.seguridadweb.repository.TareaRepository;

@Service
public class TareaService {

    private final TareaRepository tareaRepository;

    public TareaService(TareaRepository tareaRepository) {
        this.tareaRepository = tareaRepository;
    }

    public List<Tarea> listarDe(String propietario) {
        return tareaRepository.listarPorPropietario(propietario);
    }

    public List<Tarea> listarTodas() {
        return tareaRepository.findAll();
    }

    public Tarea crear(String titulo, String propietario) {
        return tareaRepository.save(new Tarea(titulo.strip(), propietario));
    }
}
