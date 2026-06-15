package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class SocioResponse {

    private Integer id;
    private String numeroSocio;
    private String codigoBarra;
    private String nombre;
    private String apellido;
    private String dni;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String telefono;
    private String email;
    private String fotoUrl;
    private Boolean activo;
    private String estadoMembresia;
}