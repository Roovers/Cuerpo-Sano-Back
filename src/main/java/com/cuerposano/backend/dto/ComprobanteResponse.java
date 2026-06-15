package com.cuerposano.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ComprobanteResponse {

    private Integer id;

    private Integer pagoId;

    private String numero;

    private LocalDateTime fechaEmision;

    private String detalle;

    /*
     * Campos extra para que el front pueda abrir la factura/comprobante
     * sin tener que hacer una segunda consulta al pago.
     */
    private PagoResponse pago;

    private PagoSocioResponse socio;

    private BigDecimal monto;

    private LocalDateTime fechaPago;

    private Integer medioPago;

    private String medioPagoNombre;

    public ComprobanteResponse(
            Integer id,
            Integer pagoId,
            String numero,
            LocalDateTime fechaEmision,
            String detalle
    ) {
        this.id = id;
        this.pagoId = pagoId;
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.detalle = detalle;
    }

    public ComprobanteResponse(
            Integer id,
            Integer pagoId,
            String numero,
            LocalDateTime fechaEmision,
            String detalle,
            PagoResponse pago,
            PagoSocioResponse socio,
            BigDecimal monto,
            LocalDateTime fechaPago,
            Integer medioPago,
            String medioPagoNombre
    ) {
        this.id = id;
        this.pagoId = pagoId;
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.detalle = detalle;
        this.pago = pago;
        this.socio = socio;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.medioPago = medioPago;
        this.medioPagoNombre = medioPagoNombre;
    }
}
