package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.Entrenador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EntrenadorRepository extends JpaRepository<Entrenador, Integer> {

    Optional<Entrenador> findByDni(String dni);

    boolean existsByDni(String dni);

    List<Entrenador> findByActivo(Boolean activo);

    @Query("""
            SELECT e
            FROM Entrenador e
            WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
               OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :buscar, '%'))
               OR LOWER(e.dni) LIKE LOWER(CONCAT('%', :buscar, '%'))
               OR LOWER(e.email) LIKE LOWER(CONCAT('%', :buscar, '%'))
            ORDER BY e.apellido ASC
            """)
    List<Entrenador> buscar(String buscar);

    long countByActivoTrue();
}