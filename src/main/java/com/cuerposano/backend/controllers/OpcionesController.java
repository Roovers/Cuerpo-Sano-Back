package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.OpcionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Opciones", description = "Opciones")
@RestController
@RequestMapping("/api/opciones")
public class OpcionesController {

    @GetMapping("/metodos-ingreso")
    public List<OpcionResponse> metodosIngreso() {
        return List.of(
                new OpcionResponse(1, "QR"),
                new OpcionResponse(2, "DNI"),
                new OpcionResponse(3, "Manual")
        );
    }

    @GetMapping("/medios-pago")
    public List<OpcionResponse> mediosPago() {
        return List.of(
                new OpcionResponse(1, "Efectivo"),
                new OpcionResponse(2, "Débito"),
                new OpcionResponse(3, "Crédito"),
                new OpcionResponse(4, "Transferencia"),
                new OpcionResponse(5, "QR")
        );
    }

    @GetMapping("/estados-membresia")
    public List<OpcionResponse> estadosMembresia() {
        return List.of(
                new OpcionResponse(1, "PendientePago"),
                new OpcionResponse(2, "Activa"),
                new OpcionResponse(3, "Vencida"),
                new OpcionResponse(4, "Cancelada")
        );
    }
}