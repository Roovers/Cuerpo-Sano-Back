package com.cuerposano.backend.repositories;

import com.cuerposano.backend.entities.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Integer> {

    List<Comprobante> findByPagoId(Integer pagoId);

    Optional<Comprobante> findFirstByPagoIdOrderByFechaEmisionDesc(Integer pagoId);

    Optional<Comprobante> findByNumero(String numero);

    boolean existsByPagoId(Integer pagoId);
}
