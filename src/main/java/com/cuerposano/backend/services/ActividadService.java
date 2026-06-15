package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.ActividadRequest;
import com.cuerposano.backend.dto.ActividadResponse;
import com.cuerposano.backend.entities.Actividad;
import com.cuerposano.backend.repositories.ActividadRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActividadService {

    private final ActividadRepository actividadRepository;

    public ActividadService(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    public List<ActividadResponse> listar(Boolean activa) {
        List<Actividad> actividades;

        if (activa != null) {
            actividades = actividadRepository.findByActiva(activa);
        } else {
            actividades = actividadRepository.findAll();
        }

        return actividades.stream()
                .map(this::toResponse)
                .toList();
    }

    public ActividadResponse obtenerPorId(Integer id) {
        Actividad actividad = buscarEntidadPorId(id);
        return toResponse(actividad);
    }

    public ActividadResponse crear(ActividadRequest request) {
        Actividad actividad = new Actividad();
        actividad.setNombre(request.getNombre());
        actividad.setDescripcion(request.getDescripcion());
        actividad.setCupoMaximo(request.getCupoMaximo());
        actividad.setActiva(request.getActiva());

        Actividad guardada = actividadRepository.save(actividad);
        return toResponse(guardada);
    }

    public ActividadResponse actualizar(Integer id, ActividadRequest request) {
        Actividad actividad = buscarEntidadPorId(id);

        actividad.setNombre(request.getNombre());
        actividad.setDescripcion(request.getDescripcion());
        actividad.setCupoMaximo(request.getCupoMaximo());
        actividad.setActiva(request.getActiva());

        Actividad actualizada = actividadRepository.save(actividad);
        return toResponse(actualizada);
    }

    public void eliminar(Integer id) {
        Actividad actividad = buscarEntidadPorId(id);

        // Lo hacemos como baja lógica para evitar romper horarios/clases asociados.
        actividad.setActiva(false);
        actividadRepository.save(actividad);
    }

    private Actividad buscarEntidadPorId(Integer id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la actividad con id: " + id));
    }

    private ActividadResponse toResponse(Actividad actividad) {
        return new ActividadResponse(
                actividad.getId(),
                actividad.getNombre(),
                actividad.getDescripcion(),
                actividad.getCupoMaximo(),
                actividad.getActiva()
        );
    }
}