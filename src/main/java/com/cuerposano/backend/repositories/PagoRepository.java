package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findBySocioId(Integer socioId);

    List<Pago> findByFechaPagoBetween(LocalDateTime desde, LocalDateTime hasta);

    List<Pago> findBySocioIdAndFechaPagoBetween(
            Integer socioId,
            LocalDateTime desde,
            LocalDateTime hasta
    );
}