package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class TipoMembresiaResponse {

    private Integer id;
    private String nombre;
    private Integer duracionDias;
    private BigDecimal precio;
    private String descripcion;
    private Boolean activa;
}