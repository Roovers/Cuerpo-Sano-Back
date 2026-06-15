package com.cuerposano.backend.controllers;

import com.cuerposano.backend.dto.CambiarPasswordRequest;
import com.cuerposano.backend.dto.CambiarPasswordResponse;
import com.cuerposano.backend.dto.LoginRequest;
import com.cuerposano.backend.dto.LoginResponse;
import com.cuerposano.backend.dto.RestablecerPasswordRequest;
import com.cuerposano.backend.dto.RestablecerPasswordResponse;
import com.cuerposano.backend.dto.SesionActualResponse;
import com.cuerposano.backend.entities.Usuario;
import com.cuerposano.backend.services.AuthService;
import com.cuerposano.backend.services.PermisosService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "Autenticación y sesión")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PermisosService permisosService;

    public AuthController(AuthService authService, PermisosService permisosService) {
        this.authService = authService;
        this.permisosService = permisosService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public SesionActualResponse me(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        Usuario usuario = authService.obtenerUsuarioActual(authorizationHeader);
        String rol = permisosService.normalizarRol(usuario.getRol());
        Integer entrenadorId = usuario.getEntrenador() != null ? usuario.getEntrenador().getId() : null;
        String entrenadorNombre = usuario.getEntrenador() != null
                ? usuario.getEntrenador().getNombre() + " " + usuario.getEntrenador().getApellido()
                : null;

        return new SesionActualResponse(
                usuario.getId(),
                usuario.getNombreUsuario(),
                rol,
                usuario.getActivo(),
                entrenadorId,
                entrenadorNombre,
                permisosService.codigosPaginas(rol),
                permisosService.codigosAcciones(rol),
                permisosService.codigosCards(rol)
        );
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("mensaje", "Sesión cerrada. El frontend debe eliminar el token.");
    }

    @PostMapping("/cambiar-password")
    public CambiarPasswordResponse cambiarPassword(
            @RequestBody CambiarPasswordRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return authService.cambiarPassword(request, authorizationHeader);
    }

    @PostMapping("/restablecer-password")
    public RestablecerPasswordResponse restablecerPassword(
            @RequestBody RestablecerPasswordRequest request
    ) {
        return authService.restablecerPassword(request);
    }
}
