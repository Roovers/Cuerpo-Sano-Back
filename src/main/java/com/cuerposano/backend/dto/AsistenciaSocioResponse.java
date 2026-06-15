package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AsistenciaSocioResponse {

    private Integer id;
    private Integer socioId;
    private String socioNombre;
    private LocalDateTime fechaHora;
    private String metodoIngreso;
}