package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ReporteAsistenciaSocioResponse {

    private Integer id;

    private Integer socioId;

    private String socioNombre;

    private String socio;

    private String nombre;

    private String apellido;

    private String dni;

    private String fotoUrl;

    private String socioFotoUrl;

    private LocalDateTime fechaHora;

    private String metodoIngreso;
}
