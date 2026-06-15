package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AsistenciaClaseResponse {

    private Integer id;

    private Integer socioId;
    private String socioNombre;

    private Integer horarioId;

    private Integer actividadId;
    private String actividadNombre;

    private Integer entrenadorId;
    private String entrenadorNombre;

    private LocalDate fechaClase;
    private LocalDateTime fechaHora;

    private String estado;
}