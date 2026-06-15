package com.cuerposano.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PagoRequest {

    @NotNull(message = "El socio es obligatorio")
    private Integer socioId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    /*
     * 1 = EFECTIVO
     * 2 = DEBITO
     * 3 = CREDITO
     * 4 = TRANSFERENCIA
     * 5 = QR
     */
    @NotNull(message = "El medio de pago es obligatorio")
    private Integer medioPago;

    private String observacion;
}