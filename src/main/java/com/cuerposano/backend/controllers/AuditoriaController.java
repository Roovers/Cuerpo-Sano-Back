package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.AuditLogPageResponse;
import com.cuerposano.backend.dto.AuditLogResponse;
import com.cuerposano.backend.services.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auditoría", description = "Logs y trazabilidad del sistema")
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditLogService auditLogService;

    public AuditoriaController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/logs")
    public AuditLogPageResponse listarLogs(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String resultado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return auditLogService.listar(desde, hasta, usuario, modulo, accion, resultado, page, size);
    }

    @GetMapping("/logs/{id}")
    public AuditLogResponse obtenerLog(@PathVariable Integer id) {
        return auditLogService.obtenerPorId(id);
    }
}
