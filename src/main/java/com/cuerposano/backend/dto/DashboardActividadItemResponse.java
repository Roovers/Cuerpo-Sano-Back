package com.cuerposano.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DashboardActividadItemResponse {

    private String tipo;

    private String descripcion;

    private String socio;

    private LocalDateTime fecha;

    private LocalDateTime fechaHora;

    private BigDecimal monto;

    private String numeroSocio;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private String actividad;

    private String entrenador;

    private String horario;
}
