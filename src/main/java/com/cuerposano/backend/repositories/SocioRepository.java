package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SocioRepository extends JpaRepository<Socio, Integer> {

    Optional<Socio> findByDni(String dni);

    Optional<Socio> findByNumeroSocio(String numeroSocio);

    Optional<Socio> findByCodigoBarra(String codigoBarra);

    boolean existsByDni(String dni);

    @Query("""
            SELECT s
            FROM Socio s
            WHERE LOWER(s.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
               OR LOWER(s.apellido) LIKE LOWER(CONCAT('%', :buscar, '%'))
               OR LOWER(s.dni) LIKE LOWER(CONCAT('%', :buscar, '%'))
               OR LOWER(s.numeroSocio) LIKE LOWER(CONCAT('%', :buscar, '%'))
            ORDER BY s.apellido ASC
            """)
    List<Socio> buscar(String buscar);

    long countByActivoTrue();
}