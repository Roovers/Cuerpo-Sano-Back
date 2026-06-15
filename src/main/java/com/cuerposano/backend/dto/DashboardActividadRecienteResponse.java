package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DashboardActividadRecienteResponse {

    private DashboardActividadItemResponse ultimoPago;

    private DashboardActividadItemResponse ultimaAsistencia;

    private DashboardActividadItemResponse ultimaMembresiaActivada;

    private DashboardActividadItemResponse ultimoSocio;

    private DashboardActividadItemResponse proximaClase;

    private List<DashboardActividadItemResponse> clasesEnEsteMomento;
}
