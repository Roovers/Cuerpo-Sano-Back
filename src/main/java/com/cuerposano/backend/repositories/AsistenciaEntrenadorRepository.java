package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.AsistenciaEntrenador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AsistenciaEntrenadorRepository extends JpaRepository<AsistenciaEntrenador, Integer> {

    List<AsistenciaEntrenador> findByEntrenadorId(Integer entrenadorId);

    List<AsistenciaEntrenador> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);
}