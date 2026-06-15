package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.DashboardActividadRecienteResponse;
import com.cuerposano.backend.dto.DashboardGraficosResponse;
import com.cuerposano.backend.dto.DashboardResumenResponse;
import com.cuerposano.backend.dto.DashboardVencimientosResponse;
import com.cuerposano.backend.services.DashboardService;
import com.cuerposano.backend.utils.DateTimeUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Dashboard", description = "Dashboard")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumen")
    public DashboardResumenResponse resumen() {
        return dashboardService.resumen();
    }

    @GetMapping("/actividad-reciente")
    public DashboardActividadRecienteResponse actividadReciente() {
        return dashboardService.actividadReciente();
    }

    @GetMapping("/graficos")
    public DashboardGraficosResponse graficos(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDateTime desdeParsed = DateTimeUtils.parseDesde(desde);
        LocalDateTime hastaParsed = DateTimeUtils.parseHasta(hasta);
        DateTimeUtils.validarRango(desdeParsed, hastaParsed);

        return dashboardService.graficos(desdeParsed, hastaParsed);
    }

    @GetMapping("/membresias-por-vencer")
    public DashboardVencimientosResponse membresiasPorVencer(
            @RequestParam(defaultValue = "7") Integer dias
    ) {
        return dashboardService.membresiasPorVencer(dias);
    }
}
