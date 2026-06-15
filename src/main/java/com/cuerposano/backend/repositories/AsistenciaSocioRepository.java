package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.AsistenciaSocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AsistenciaSocioRepository extends JpaRepository<AsistenciaSocio, Integer> {

    List<AsistenciaSocio> findBySocioId(Integer socioId);

    List<AsistenciaSocio> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);

    List<AsistenciaSocio> findAllByOrderByFechaHoraDesc();

    List<AsistenciaSocio> findByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime desde,
            LocalDateTime hasta
    );

    List<AsistenciaSocio> findByFechaHoraGreaterThanEqualOrderByFechaHoraDesc(
            LocalDateTime desde
    );

    List<AsistenciaSocio> findByFechaHoraLessThanEqualOrderByFechaHoraDesc(
            LocalDateTime hasta
    );

    /*
     * PostgreSQL puede fallar con queries del tipo:
     *
     * (:desde IS NULL OR fechaHora >= :desde)
     *
     * porque no siempre puede inferir el tipo del parámetro usado en IS NULL.
     * Por eso resolvemos el filtro con métodos concretos según vengan desde/hasta.
     */
    default List<AsistenciaSocio> filtrarPorFechaHora(
            LocalDateTime desde,
            LocalDateTime hasta
    ) {
        if (desde != null && hasta != null) {
            return findByFechaHoraBetweenOrderByFechaHoraDesc(desde, hasta);
        }

        if (desde != null) {
            return findByFechaHoraGreaterThanEqualOrderByFechaHoraDesc(desde);
        }

        if (hasta != null) {
            return findByFechaHoraLessThanEqualOrderByFechaHoraDesc(hasta);
        }

        return findAllByOrderByFechaHoraDesc();
    }
}
