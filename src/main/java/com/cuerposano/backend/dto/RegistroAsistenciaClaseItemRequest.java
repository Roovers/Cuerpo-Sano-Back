package com.cuerposano.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroAsistenciaClaseItemRequest {

    @NotNull(message = "El socio es obligatorio")
    private Integer socioId;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}