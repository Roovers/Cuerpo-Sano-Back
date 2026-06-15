package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ActividadResponse {

    private Integer id;
    private String nombre;
    private String descripcion;
    private Integer cupoMaximo;
    private Boolean activa;
}