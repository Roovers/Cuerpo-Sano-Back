package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.ComprobanteRequest;
import com.cuerposano.backend.dto.ComprobanteResponse;
import com.cuerposano.backend.services.ComprobanteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Comprobantes", description = "Comprobantes")
@RestController
@RequestMapping("/api/comprobantes")
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    public ComprobanteController(ComprobanteService comprobanteService) {
        this.comprobanteService = comprobanteService;
    }

    @GetMapping
    public List<ComprobanteResponse> listar(@RequestParam(required = false) Integer pagoId) {
        return comprobanteService.listar(pagoId);
    }

    @GetMapping("/{id}")
    public ComprobanteResponse obtenerPorId(@PathVariable Integer id) {
        return comprobanteService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public ComprobanteResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ComprobanteRequest request
    ) {
        return comprobanteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        comprobanteService.eliminar(id);
    }
}