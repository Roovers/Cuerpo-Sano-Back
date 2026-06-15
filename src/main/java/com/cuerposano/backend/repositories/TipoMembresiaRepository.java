package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.TipoMembresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoMembresiaRepository extends JpaRepository<TipoMembresia, Integer> {

    List<TipoMembresia> findByActiva(Boolean activa);
}