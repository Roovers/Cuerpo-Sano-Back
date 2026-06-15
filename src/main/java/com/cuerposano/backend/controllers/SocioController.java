package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.SocioCarnetResponse;
import com.cuerposano.backend.dto.SocioRequest;
import com.cuerposano.backend.dto.SocioResponse;
import com.cuerposano.backend.services.SocioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Socios", description = "Socios")
@RestController
@RequestMapping("/api/socios")
public class SocioController {

    private final SocioService socioService;

    public SocioController(SocioService socioService) {
        this.socioService = socioService;
    }

    @GetMapping
    public List<SocioResponse> listar(@RequestParam(required = false) String buscar) {
        return socioService.listar(buscar);
    }

    @GetMapping("/{id}")
    public SocioResponse obtenerPorId(@PathVariable Integer id) {
        return socioService.obtenerPorId(id);
    }

    @PostMapping
    public SocioResponse crear(@Valid @RequestBody SocioRequest request) {
        return socioService.crear(request);
    }

    @PutMapping("/{id}")
    public SocioResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody SocioRequest request
    ) {
        return socioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        socioService.eliminar(id);
    }

    @GetMapping("/{id}/carnet")
    public SocioCarnetResponse obtenerCarnet(@PathVariable Integer id) {
        return socioService.obtenerCarnet(id);
    }
}