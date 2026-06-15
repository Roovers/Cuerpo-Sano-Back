package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Integer> {

    List<Actividad> findByActiva(Boolean activa);

    List<Actividad> findByNombreContainingIgnoreCase(String nombre);
}