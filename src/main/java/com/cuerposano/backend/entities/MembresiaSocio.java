package com.cuerposano.backend.entities;

import com.cuerposano.backend.enums.EstadoMembresiaSocio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "membresias_socios")
@Getter
@Setter
@NoArgsConstructor
public class MembresiaSocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_membresia_id", nullable = false)
    private TipoMembresia tipoMembresia;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMembresiaSocio estado = EstadoMembresiaSocio.PENDIENTE_PAGO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    private Pago pago;
}