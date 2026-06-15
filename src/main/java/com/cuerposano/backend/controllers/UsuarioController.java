package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.RolUsuarioResponse;
import com.cuerposano.backend.dto.UsuarioRequest;
import com.cuerposano.backend.dto.UsuarioResponse;
import com.cuerposano.backend.dto.UsuarioUpdateRequest;
import com.cuerposano.backend.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Usuarios", description = "Usuarios y roles")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/roles")
    public List<RolUsuarioResponse> listarRoles() {
        return usuarioService.listarRoles();
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtenerPorId(@PathVariable Integer id) {
        return usuarioService.obtenerPorId(id);
    }

    @PostMapping
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.crear(request);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioUpdateRequest request
    ) {
        return usuarioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
    }
}
