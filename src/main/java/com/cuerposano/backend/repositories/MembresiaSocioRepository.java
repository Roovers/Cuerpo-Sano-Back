package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.MembresiaSocio;
import com.cuerposano.backend.entities.Socio;
import com.cuerposano.backend.enums.EstadoMembresiaSocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembresiaSocioRepository extends JpaRepository<MembresiaSocio, Integer> {

    List<MembresiaSocio> findBySocioId(Integer socioId);

    List<MembresiaSocio> findByEstado(EstadoMembresiaSocio estado);

    Optional<MembresiaSocio> findFirstBySocioAndEstadoOrderByFechaFinDesc(
            Socio socio,
            EstadoMembresiaSocio estado
    );

    List<MembresiaSocio> findByFechaFinBetween(LocalDate desde, LocalDate hasta);

    Optional<MembresiaSocio> findFirstBySocioIdOrderByFechaFinDesc(Integer socioId);

    boolean existsByTipoMembresiaId(Integer tipoMembresiaId);
}