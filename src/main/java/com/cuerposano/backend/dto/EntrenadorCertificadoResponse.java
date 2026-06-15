package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EntrenadorCertificadoResponse {

    private Integer entrenadorId;
    private String nombre;
    private String apellido;
    private Boolean certificado;
}