package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.ComprobanteRequest;
import com.cuerposano.backend.dto.ComprobanteResponse;
import com.cuerposano.backend.dto.PagoRequest;
import com.cuerposano.backend.dto.PagoResponse;
import com.cuerposano.backend.services.PagoService;
import com.cuerposano.backend.utils.DateTimeUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Pagos", description = "Pagos")
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public List<PagoResponse> listar(@RequestParam(required = false) Integer socioId) {
        return pagoService.listar(socioId);
    }

    @PostMapping
    public PagoResponse crear(@Valid @RequestBody PagoRequest request) {
        return pagoService.crear(request);
    }

    @GetMapping("/{id}")
    public PagoResponse obtenerPorId(@PathVariable Integer id) {
        return pagoService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public PagoResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody PagoRequest request
    ) {
        return pagoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        pagoService.eliminar(id);
    }

    @PostMapping("/{pagoId}/comprobante")
    public ComprobanteResponse crearComprobante(
            @PathVariable Integer pagoId,
            @Valid @RequestBody ComprobanteRequest request
    ) {
        return pagoService.crearComprobante(pagoId, request);
    }

    @GetMapping("/historial")
    public List<PagoResponse> historial(
            @RequestParam(required = false) Integer socioId,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDateTime desdeParsed = DateTimeUtils.parseDesde(desde);
        LocalDateTime hastaParsed = DateTimeUtils.parseHasta(hasta);
        DateTimeUtils.validarRango(desdeParsed, hastaParsed);

        return pagoService.historial(socioId, desdeParsed, hastaParsed);
    }
}
