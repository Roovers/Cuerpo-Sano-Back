package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.HorarioRequest;
import com.cuerposano.backend.dto.HorarioResponse;
import com.cuerposano.backend.services.HorarioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Horarios", description = "Horarios")
@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping
    public List<HorarioResponse> listar(
            @RequestParam(required = false) Integer actividadId,
            @RequestParam(required = false) Integer entrenadorId,
            @RequestParam(required = false) Integer dia,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaDesde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaHasta
    ) {
        return horarioService.listar(
                actividadId,
                entrenadorId,
                dia,
                activo,
                fechaDesde,
                fechaHasta
        );
    }

    @PostMapping
    public HorarioResponse crear(@Valid @RequestBody HorarioRequest request) {
        return horarioService.crear(request);
    }

    @PutMapping("/{id}")
    public HorarioResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody HorarioRequest request
    ) {
        return horarioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        horarioService.eliminar(id);
    }
}
