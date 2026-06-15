package com.cuerposano.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InscripcionClaseRequest {

    @NotNull(message = "El socio es obligatorio")
    private Integer socioId;

    @NotNull(message = "El horario es obligatorio")
    private Integer horarioId;
}