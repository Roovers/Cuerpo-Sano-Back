package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AsistenciaEntrenadorResponse {

    private Integer id;
    private Integer entrenadorId;
    private String entrenadorNombre;
    private LocalDateTime fechaHora;
    private String observacion;
}