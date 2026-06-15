package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class InscripcionClaseResponse {

    private Integer id;

    private Integer socioId;
    private String socioNombre;

    private Integer horarioId;

    private Integer actividadId;
    private String actividadNombre;

    private Integer entrenadorId;
    private String entrenadorNombre;

    private Integer diaSemana;
    private String diaSemanaNombre;

    private LocalTime horaInicio;
    private LocalTime horaFin;

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;

    private LocalDateTime fechaInscripcion;

    private Boolean activa;
}