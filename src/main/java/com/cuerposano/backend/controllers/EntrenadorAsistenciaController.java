package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.AsistenciaEntrenadorRequest;
import com.cuerposano.backend.dto.AsistenciaEntrenadorResponse;
import com.cuerposano.backend.services.AsistenciaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Asistencias a Clases", description = "Presentismo")
@RestController
@RequestMapping("/api/entrenadores/asistencias")
public class EntrenadorAsistenciaController {

    private final AsistenciaService asistenciaService;

    public EntrenadorAsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @PostMapping
    public AsistenciaEntrenadorResponse registrarAsistenciaEntrenador(
            @Valid @RequestBody AsistenciaEntrenadorRequest request
    ) {
        return asistenciaService.registrarAsistenciaEntrenador(request);
    }
}