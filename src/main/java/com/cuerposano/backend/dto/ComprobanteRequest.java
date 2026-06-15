package com.cuerposano.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComprobanteRequest {

    @NotNull(message = "El pago es obligatorio")
    private Integer pagoId;

    private String detalle;
}