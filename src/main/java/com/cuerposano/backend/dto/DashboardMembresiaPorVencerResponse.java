package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class DashboardMembresiaPorVencerResponse {

    private Integer membresiaId;

    private Integer socioId;

    private String socio;

    private String socioNombre;

    private String nombre;

    private String apellido;

    private String socioTelefono;

    private String telefono;

    private String socioEmail;

    private String email;

    private String numeroSocio;

    private String tipoMembresia;

    private LocalDate fechaFin;

    private LocalDate fechaVencimiento;

    private Long diasRestantes;
}
