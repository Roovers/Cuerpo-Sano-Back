package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.*;
import com.cuerposano.backend.services.AsistenciaService;
import com.cuerposano.backend.utils.DateTimeUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Asistencias", description = "Asistencias")
@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @PostMapping("/socios")
    public AsistenciaSocioResponse registrarAsistenciaSocio(
            @Valid @RequestBody AsistenciaSocioRequest request
    ) {
        return asistenciaService.registrarAsistenciaSocio(request);
    }

    @PostMapping("/clases")
    public AsistenciaClaseResponse registrarAsistenciaClase(
            @Valid @RequestBody AsistenciaClaseRequest request
    ) {
        return asistenciaService.registrarAsistenciaClase(request);
    }

    @GetMapping("/clases")
    public List<AsistenciaClaseResponse> listarAsistenciasClase(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDateTime desdeParsed = DateTimeUtils.parseDesde(desde);
        LocalDateTime hastaParsed = DateTimeUtils.parseHasta(hasta);
        DateTimeUtils.validarRango(desdeParsed, hastaParsed);

        return asistenciaService.listarAsistenciasClase(desdeParsed, hastaParsed);
    }

    @PutMapping("/clases/registro")
    public RegistroAsistenciaClaseResponse guardarRegistroClase(
            @Valid @RequestBody RegistroAsistenciaClaseRequest request
    ) {
        return asistenciaService.guardarRegistroClase(request);
    }
}
