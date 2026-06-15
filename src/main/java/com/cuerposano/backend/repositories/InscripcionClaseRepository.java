package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.InscripcionClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InscripcionClaseRepository extends JpaRepository<InscripcionClase, Integer> {

    List<InscripcionClase> findBySocioId(Integer socioId);

    List<InscripcionClase> findByHorarioId(Integer horarioId);

    Optional<InscripcionClase> findBySocioIdAndHorarioId(Integer socioId, Integer horarioId);

    boolean existsBySocioIdAndHorarioId(Integer socioId, Integer horarioId);

    boolean existsBySocioIdAndHorarioIdAndActivaTrue(Integer socioId, Integer horarioId);

    long countByHorarioIdAndActivaTrue(Integer horarioId);

    @Query("""
            SELECT i
            FROM InscripcionClase i
            WHERE (:socioId IS NULL OR i.socio.id = :socioId)
              AND (:horarioId IS NULL OR i.horario.id = :horarioId)
              AND (:activa IS NULL OR i.activa = :activa)
            ORDER BY i.fechaInscripcion DESC
            """)
    List<InscripcionClase> filtrar(
            @Param("socioId") Integer socioId,
            @Param("horarioId") Integer horarioId,
            @Param("activa") Boolean activa
    );
}
