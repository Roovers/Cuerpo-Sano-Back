package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.RolPermisosResponse;
import com.cuerposano.backend.services.PermisosService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Configuración", description = "Roles, permisos y seguridad")
@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final PermisosService permisosService;

    public ConfiguracionController(PermisosService permisosService) {
        this.permisosService = permisosService;
    }

    @GetMapping("/roles-permisos")
    public List<RolPermisosResponse> listarRolesPermisos() {
        return permisosService.listarRolesPermisos();
    }

    @GetMapping("/roles-permisos/{rol}")
    public RolPermisosResponse obtenerRolPermisos(@PathVariable String rol) {
        return permisosService.obtenerPorRol(rol);
    }
}
