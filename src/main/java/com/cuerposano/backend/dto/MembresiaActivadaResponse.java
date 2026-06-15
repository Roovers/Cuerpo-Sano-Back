package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MembresiaActivadaResponse {

    private String mensaje;
    private MembresiaSocioResponse membresia;
    private String comprobante;
}