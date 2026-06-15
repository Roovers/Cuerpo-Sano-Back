package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class MembresiaSocioResponse {

    private Integer id;

    private Integer socioId;
    private String socioNombre;

    private Integer tipoMembresiaId;
    private String tipoMembresiaNombre;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String estado;

    private Integer pagoId;
}