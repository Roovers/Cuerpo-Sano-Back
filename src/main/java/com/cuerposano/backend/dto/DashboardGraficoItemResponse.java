package com.cuerposano.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DashboardGraficoItemResponse {

    private String fecha;

    private String dia;

    private String actividad;

    private String estado;

    private String nombre;

    private Long total;

    private Long cantidad;

    private BigDecimal monto;

    private BigDecimal valor;

    public static DashboardGraficoItemResponse fechaCantidad(String fecha, Long cantidad) {
        DashboardGraficoItemResponse item = new DashboardGraficoItemResponse();
        item.setFecha(fecha);
        item.setDia(fecha);
        item.setCantidad(cantidad);
        item.setTotal(cantidad);
        return item;
    }

    public static DashboardGraficoItemResponse fechaMonto(String fecha, BigDecimal monto) {
        DashboardGraficoItemResponse item = new DashboardGraficoItemResponse();
        item.setFecha(fecha);
        item.setDia(fecha);
        item.setMonto(monto);
        item.setValor(monto);
        return item;
    }

    public static DashboardGraficoItemResponse nombreCantidad(String nombre, Long cantidad) {
        DashboardGraficoItemResponse item = new DashboardGraficoItemResponse();
        item.setNombre(nombre);
        item.setActividad(nombre);
        item.setEstado(nombre);
        item.setCantidad(cantidad);
        item.setTotal(cantidad);
        return item;
    }
}
