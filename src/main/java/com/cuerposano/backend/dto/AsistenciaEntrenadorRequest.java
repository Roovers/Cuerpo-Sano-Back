package com.cuerposano.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsistenciaEntrenadorRequest {

    @NotNull(message = "El entrenador es obligatorio")
    private Integer entrenadorId;

    private String observacion;
}