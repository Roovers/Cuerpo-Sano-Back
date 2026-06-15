package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class DashboardResumenResponse {

    private Long totalSocios;
    private Long sociosActivos;

    private Long membresiasActivas;
    private Long membresiasPendientesPago;
    private Long membresiasVencidas;
    private Long membresiasPorVencer;

    private Long entrenadoresActivos;
    private Long clasesActivas;
    private Long clasesProgramadasHoy;
    private Long actividadesActivas;

    private Long pagosDelDia;
    private BigDecimal ingresosMesActual;
    private BigDecimal recaudacionDelMes;

    private Long asistenciasSociosHoy;
    private Long asistenciasClasesHoy;
}
