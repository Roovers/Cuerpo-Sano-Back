package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class HorarioResponse {

    private Integer id;

    private Integer diaSemana;
    private String diaSemanaNombre;

    private LocalTime horaInicio;
    private LocalTime horaFin;

    private Integer actividadId;
    private String actividadNombre;

    private Integer entrenadorId;
    private String entrenadorNombre;
    private String entrenadorFotoUrl;

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;

    private Boolean activo;
}