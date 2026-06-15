package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DashboardVencimientosResponse {

    private Integer dias;

    private Integer cantidad;

    private List<DashboardMembresiaPorVencerResponse> membresias;
}
