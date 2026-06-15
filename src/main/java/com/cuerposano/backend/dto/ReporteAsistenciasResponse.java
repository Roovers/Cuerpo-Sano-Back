package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ReporteAsistenciasResponse<T> {

    private Integer cantidad;

    private Map<String, Object> filtros;

    private List<T> asistencias;
}
