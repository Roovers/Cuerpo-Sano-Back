package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DashboardGraficosResponse {

    private DashboardFiltrosResponse filtros;

    private List<DashboardGraficoItemResponse> recaudacionPorDia;

    private List<DashboardGraficoItemResponse> asistenciasPorDia;

    private List<DashboardGraficoItemResponse> asistenciasPorActividad;

    private List<DashboardGraficoItemResponse> membresiasPorEstado;
}
