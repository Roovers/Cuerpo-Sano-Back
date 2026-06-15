package com.cuerposano.backend.entities;

import com.cuerposano.backend.enums.MedioPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false)
    private MedioPago medioPago;

    @Column(columnDefinition = "TEXT")
    private String observacion;
}