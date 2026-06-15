package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class RegistroAsistenciaClaseResponse {

    private Integer horarioId;
    private LocalDate fechaClase;
    private Integer presentes;
    private Integer ausentes;
    private Integer total;
}