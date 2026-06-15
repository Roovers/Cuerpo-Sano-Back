package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SocioCarnetResponse {

    private Integer id;
    private String numeroSocio;
    private String nombre;
    private String apellido;
    private String fotoUrl;
    private String codigoBarra;
}