package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.InscripcionClaseRequest;
import com.cuerposano.backend.dto.InscripcionClaseResponse;
import com.cuerposano.backend.services.InscripcionClaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Inscripciones a clases", description = "Inscripciones a clases")
@RestController
@RequestMapping("/api/inscripciones-clases")
public class InscripcionClaseController {

    private final InscripcionClaseService inscripcionClaseService;

    public InscripcionClaseController(InscripcionClaseService inscripcionClaseService) {
        this.inscripcionClaseService = inscripcionClaseService;
    }

    @GetMapping
    public List<InscripcionClaseResponse> listar(
            @RequestParam(required = false) Integer socioId,
            @RequestParam(required = false) Integer horarioId,
            @RequestParam(required = false) Boolean activa
    ) {
        return inscripcionClaseService.listar(socioId, horarioId, activa);
    }

    @PostMapping
    public InscripcionClaseResponse crear(
            @Valid @RequestBody InscripcionClaseRequest request
    ) {
        return inscripcionClaseService.crear(request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        inscripcionClaseService.eliminar(id);
    }
}
