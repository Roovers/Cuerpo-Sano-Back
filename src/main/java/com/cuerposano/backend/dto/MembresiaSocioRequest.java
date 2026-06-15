package com.cuerposano.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MembresiaSocioRequest {

    @NotNull(message = "El socio es obligatorio")
    private Integer socioId;

    @NotNull(message = "El tipo de membresía es obligatorio")
    private Integer tipoMembresiaId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;
}