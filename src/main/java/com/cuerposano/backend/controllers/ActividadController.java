package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.ActividadRequest;
import com.cuerposano.backend.dto.ActividadResponse;
import com.cuerposano.backend.services.ActividadService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Actividades", description = "Actividades")
@RestController
@RequestMapping("/api/actividades")
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping
    public List<ActividadResponse> listar(@RequestParam(required = false) Boolean activa) {
        return actividadService.listar(activa);
    }

    @GetMapping("/{id}")
    public ActividadResponse obtenerPorId(@PathVariable Integer id) {
        return actividadService.obtenerPorId(id);
    }

    @PostMapping
    public ActividadResponse crear(@Valid @RequestBody ActividadRequest request) {
        return actividadService.crear(request);
    }

    @PutMapping("/{id}")
    public ActividadResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ActividadRequest request
    ) {
        return actividadService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        actividadService.eliminar(id);
    }
}