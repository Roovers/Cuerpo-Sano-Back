package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.EntrenadorCertificadoResponse;
import com.cuerposano.backend.dto.EntrenadorRequest;
import com.cuerposano.backend.dto.EntrenadorResponse;
import com.cuerposano.backend.services.EntrenadorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Entrenadores", description = "Entrenadores")
@RestController
@RequestMapping("/api/entrenadores")
public class EntrenadorController {

    private final EntrenadorService entrenadorService;

    public EntrenadorController(EntrenadorService entrenadorService) {
        this.entrenadorService = entrenadorService;
    }

    @GetMapping
    public List<EntrenadorResponse> listar(@RequestParam(required = false) String buscar) {
        return entrenadorService.listar(buscar);
    }

    @GetMapping("/{id}")
    public EntrenadorResponse obtenerPorId(@PathVariable Integer id) {
        return entrenadorService.obtenerPorId(id);
    }

    @PostMapping
    public EntrenadorResponse crear(@Valid @RequestBody EntrenadorRequest request) {
        return entrenadorService.crear(request);
    }

    @PutMapping("/{id}")
    public EntrenadorResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody EntrenadorRequest request
    ) {
        return entrenadorService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        entrenadorService.eliminar(id);
    }

    @GetMapping("/{id}/certificado")
    public EntrenadorCertificadoResponse obtenerCertificado(@PathVariable Integer id) {
        return entrenadorService.obtenerCertificado(id);
    }
}