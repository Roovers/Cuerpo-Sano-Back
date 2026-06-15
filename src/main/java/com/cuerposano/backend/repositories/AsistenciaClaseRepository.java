package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.AsistenciaClase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AsistenciaClaseRepository extends JpaRepository<AsistenciaClase, Integer> {

    List<AsistenciaClase> findByHorarioId(Integer horarioId);

    List<AsistenciaClase> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);

    List<AsistenciaClase> findByFechaClaseBetween(LocalDate desde, LocalDate hasta);

    Optional<AsistenciaClase> findBySocioIdAndHorarioIdAndFechaClase(
            Integer socioId,
            Integer horarioId,
            LocalDate fechaClase
    );

    List<AsistenciaClase> findAllByOrderByFechaClaseDescFechaHoraDesc();

    List<AsistenciaClase> findByFechaHoraBetweenOrderByFechaClaseDescFechaHoraDesc(
            LocalDateTime desde,
            LocalDateTime hasta
    );

    List<AsistenciaClase> findByFechaHoraGreaterThanEqualOrderByFechaClaseDescFechaHoraDesc(
            LocalDateTime desde
    );

    List<AsistenciaClase> findByFechaHoraLessThanEqualOrderByFechaClaseDescFechaHoraDesc(
            LocalDateTime hasta
    );

    /*
     * Mismo criterio que en AsistenciaSocioRepository:
     * evitamos JPQL con (:desde IS NULL OR ...) porque PostgreSQL puede
     * no inferir el tipo del parámetro.
     */
    default List<AsistenciaClase> filtrarPorFechaHora(
            LocalDateTime desde,
            LocalDateTime hasta
    ) {
        if (desde != null && hasta != null) {
            return findByFechaHoraBetweenOrderByFechaClaseDescFechaHoraDesc(desde, hasta);
        }

        if (desde != null) {
            return findByFechaHoraGreaterThanEqualOrderByFechaClaseDescFechaHoraDesc(desde);
        }

        if (hasta != null) {
            return findByFechaHoraLessThanEqualOrderByFechaClaseDescFechaHoraDesc(hasta);
        }

        return findAllByOrderByFechaClaseDescFechaHoraDesc();
    }
}
