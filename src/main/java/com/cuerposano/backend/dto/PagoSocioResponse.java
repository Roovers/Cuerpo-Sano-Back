package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoSocioResponse {

    private Integer id;

    private String numeroSocio;

    private String nombre;

    private String apellido;

    private String dni;

    private String email;
}
