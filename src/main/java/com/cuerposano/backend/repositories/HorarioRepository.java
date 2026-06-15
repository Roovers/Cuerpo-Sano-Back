package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Integer> {

    List<Horario> findByActividadId(Integer actividadId);

    List<Horario> findByEntrenadorId(Integer entrenadorId);

    List<Horario> findByDiaSemana(Integer diaSemana);

    List<Horario> findByActivo(Boolean activo);

    @Query("""
            SELECT h
            FROM Horario h
            WHERE (:actividadId IS NULL OR h.actividad.id = :actividadId)
              AND (:entrenadorId IS NULL OR h.entrenador.id = :entrenadorId)
              AND (:diaSemana IS NULL OR h.diaSemana = :diaSemana)
              AND (:activo IS NULL OR h.activo = :activo)
              AND (:fechaDesde IS NULL OR h.fechaHasta >= :fechaDesde)
              AND (:fechaHasta IS NULL OR h.fechaDesde <= :fechaHasta)
            ORDER BY h.diaSemana ASC, h.horaInicio ASC
            """)
    List<Horario> filtrar(
            @Param("actividadId") Integer actividadId,
            @Param("entrenadorId") Integer entrenadorId,
            @Param("diaSemana") Integer diaSemana,
            @Param("activo") Boolean activo,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta
    );

    @Query("""
            SELECT h
            FROM Horario h
            WHERE h.activo = true
              AND (:idIgnorar IS NULL OR h.id <> :idIgnorar)
              AND h.diaSemana = :diaSemana
              AND (h.entrenador.id = :entrenadorId OR h.actividad.id = :actividadId)
              AND h.horaInicio < :horaFin
              AND h.horaFin > :horaInicio
              AND h.fechaDesde <= :fechaHasta
              AND h.fechaHasta >= :fechaDesde
            """)
    List<Horario> buscarSuperposiciones(
            @Param("idIgnorar") Integer idIgnorar,
            @Param("diaSemana") Integer diaSemana,
            @Param("actividadId") Integer actividadId,
            @Param("entrenadorId") Integer entrenadorId,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta
    );

    long countByActivoTrue();
}
