package com.cuerposano.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PagoResponse {

    private Integer id;

    private Integer socioId;

    private String socioNombre;

    private BigDecimal monto;

    private LocalDateTime fechaPago;

    private Integer medioPago;

    private String medioPagoNombre;

    private String observacion;

    private PagoSocioResponse socio;

    private Boolean tieneComprobante;

    private Integer comprobanteId;

    private String comprobanteNumero;

    public PagoResponse(
            Integer id,
            Integer socioId,
            String socioNombre,
            BigDecimal monto,
            LocalDateTime fechaPago,
            Integer medioPago,
            String medioPagoNombre,
            String observacion
    ) {
        this(
                id,
                socioId,
                socioNombre,
                monto,
                fechaPago,
                medioPago,
                medioPagoNombre,
                observacion,
                null,
                false,
                null,
                null
        );
    }

    public PagoResponse(
            Integer id,
            Integer socioId,
            String socioNombre,
            BigDecimal monto,
            LocalDateTime fechaPago,
            Integer medioPago,
            String medioPagoNombre,
            String observacion,
            PagoSocioResponse socio,
            Boolean tieneComprobante,
            Integer comprobanteId,
            String comprobanteNumero
    ) {
        this.id = id;
        this.socioId = socioId;
        this.socioNombre = socioNombre;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.medioPago = medioPago;
        this.medioPagoNombre = medioPagoNombre;
        this.observacion = observacion;
        this.socio = socio;
        this.tieneComprobante = tieneComprobante;
        this.comprobanteId = comprobanteId;
        this.comprobanteNumero = comprobanteNumero;
    }
}
