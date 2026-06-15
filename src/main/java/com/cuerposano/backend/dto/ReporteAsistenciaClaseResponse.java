package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ReporteAsistenciaClaseResponse {

    private Integer id;

    private Integer socioId;

    private String socioNombre;

    private String socio;

    private String nombre;

    private String apellido;

    private String dni;

    private String fotoUrl;

    private String socioFotoUrl;

    private Integer horarioId;

    private Integer actividadId;

    private String actividadNombre;

    private String actividad;

    private Integer entrenadorId;

    private String entrenadorNombre;

    private String entrenador;

    private String entrenadorFotoUrl;

    private LocalDate fechaClase;

    private LocalDateTime fechaHora;

    private String dia;

    private String horaInicio;

    private String horaFin;

    private String estado;
}
