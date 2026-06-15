package com.cuerposano.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsistenciaSocioRequest {

    @NotNull(message = "El socio es obligatorio")
    private Integer socioId;

    private String metodoIngreso;
}