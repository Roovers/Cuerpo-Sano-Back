package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.ReporteAsistenciaClaseResponse;
import com.cuerposano.backend.dto.ReporteAsistenciaSocioResponse;
import com.cuerposano.backend.dto.ReporteAsistenciasResponse;
import com.cuerposano.backend.services.ReporteService;
import com.cuerposano.backend.utils.DateTimeUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "Reportes", description = "Reportes")
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/asistencia-socios")
    public ReporteAsistenciasResponse<ReporteAsistenciaSocioResponse> reporteAsistenciaSocios(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDateTime desdeParsed = DateTimeUtils.parseDesde(desde);
        LocalDateTime hastaParsed = DateTimeUtils.parseHasta(hasta);
        DateTimeUtils.validarRango(desdeParsed, hastaParsed);

        var asistencias = reporteService.reporteAsistenciaSocios(desdeParsed, hastaParsed);

        return new ReporteAsistenciasResponse<>(
                asistencias.size(),
                Map.of(
                        "desde", desdeParsed != null ? desdeParsed.toString() : "",
                        "hasta", hastaParsed != null ? hastaParsed.toString() : ""
                ),
                asistencias
        );
    }

    @GetMapping("/asistencia-clases")
    public ReporteAsistenciasResponse<ReporteAsistenciaClaseResponse> reporteAsistenciaClases(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDateTime desdeParsed = DateTimeUtils.parseDesde(desde);
        LocalDateTime hastaParsed = DateTimeUtils.parseHasta(hasta);
        DateTimeUtils.validarRango(desdeParsed, hastaParsed);

        var asistencias = reporteService.reporteAsistenciaClases(desdeParsed, hastaParsed);

        return new ReporteAsistenciasResponse<>(
                asistencias.size(),
                Map.of(
                        "desde", desdeParsed != null ? desdeParsed.toString() : "",
                        "hasta", hastaParsed != null ? hastaParsed.toString() : ""
                ),
                asistencias
        );
    }
}
