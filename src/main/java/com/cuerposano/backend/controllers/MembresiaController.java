package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.*;
import com.cuerposano.backend.services.MembresiaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Membresías", description = "Membresías")
@RestController
@RequestMapping("/api/membresias")
public class MembresiaController {

    private final MembresiaService membresiaService;

    public MembresiaController(MembresiaService membresiaService) {
        this.membresiaService = membresiaService;
    }

    @GetMapping("/tipos")
    public List<TipoMembresiaResponse> listarTipos() {
        return membresiaService.listarTipos();
    }

    @PostMapping("/tipos")
    public TipoMembresiaResponse crearTipo(@Valid @RequestBody TipoMembresiaRequest request) {
        return membresiaService.crearTipo(request);
    }

    @PutMapping("/tipos/{id}")
    public TipoMembresiaResponse actualizarTipo(
            @PathVariable Integer id,
            @Valid @RequestBody TipoMembresiaRequest request
    ) {
        return membresiaService.actualizarTipo(id, request);
    }

    @DeleteMapping("/tipos/{id}")
    public void eliminarTipo(@PathVariable Integer id) {
        membresiaService.eliminarTipo(id);
    }

    @GetMapping("/socios/{socioId}")
    public List<MembresiaSocioResponse> historialSocio(@PathVariable Integer socioId) {
        return membresiaService.historialSocio(socioId);
    }

    @PostMapping("/socios")
    public MembresiaCreadaResponse crearMembresiaSocio(
            @Valid @RequestBody MembresiaSocioRequest request
    ) {
        return membresiaService.crearMembresiaSocio(request);
    }

    @PostMapping("/socios/{id}/activar")
    public MembresiaActivadaResponse activarMembresia(
            @PathVariable Integer id,
            @RequestParam String numeroComprobante
    ) {
        return membresiaService.activarMembresia(id, numeroComprobante);
    }

    @GetMapping("/avisos-vencimiento")
    public List<MembresiaSocioResponse> avisosVencimiento(
            @RequestParam(defaultValue = "7") Integer dias
    ) {
        return membresiaService.avisosVencimiento(dias);
    }
}