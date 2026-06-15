package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.CambiarPasswordRequest;
import com.cuerposano.backend.dto.CambiarPasswordResponse;
import com.cuerposano.backend.dto.LoginRequest;
import com.cuerposano.backend.dto.LoginResponse;
import com.cuerposano.backend.dto.RestablecerPasswordRequest;
import com.cuerposano.backend.dto.RestablecerPasswordResponse;
import com.cuerposano.backend.entities.Usuario;
import com.cuerposano.backend.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;

@Service
public class AuthService {

    private static final String PASSWORD_TEMPORAL_PREFIX = "CS-";
    private static final String PASSWORD_TEMPORAL_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final PermisosService permisosService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditLogService auditLogService,
            PermisosService permisosService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
        this.permisosService = permisosService;
    }

    public LoginResponse login(LoginRequest request) {
        validarLoginRequest(request);

        Usuario usuario = usuarioRepository.findByNombreUsuario(request.getUsuario())
                .orElseThrow(() -> {
                    auditLogService.registrarSistema(
                            request.getUsuario(),
                            null,
                            "Auth",
                            "LOGIN_FALLIDO",
                            "Usuario",
                            null,
                            "ERROR",
                            "Intento de inicio de sesión con usuario inexistente"
                    );
                    return new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Credenciales inválidas"
                    );
                });

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            auditLogService.registrar(
                    usuario,
                    "Auth",
                    "LOGIN_FALLIDO",
                    "Usuario",
                    usuario.getId(),
                    "ERROR",
                    "Intento de inicio de sesión con usuario inactivo"
            );
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas"
            );
        }

        boolean passwordOk;

        try {
            passwordOk = passwordEncoder.matches(
                    request.getPassword(),
                    usuario.getPasswordHash()
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas"
            );
        }

        if (!passwordOk) {
            auditLogService.registrar(
                    usuario,
                    "Auth",
                    "LOGIN_FALLIDO",
                    "Usuario",
                    usuario.getId(),
                    "ERROR",
                    "Contraseña incorrecta"
            );
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas"
            );
        }

        String jwt = jwtService.generarToken(usuario);

        auditLogService.registrar(
                usuario,
                "Auth",
                "LOGIN_OK",
                "Usuario",
                usuario.getId(),
                "OK",
                "Inicio de sesión correcto"
        );

        return new LoginResponse(
                jwt,
                usuario.getNombreUsuario(),
                permisosService.normalizarRol(usuario.getRol())
        );
    }

    public CambiarPasswordResponse cambiarPassword(
            CambiarPasswordRequest request,
            String authorizationHeader
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe informar la contraseña actual y la nueva contraseña"
            );
        }

        Usuario usuario = resolverUsuarioActual(request, authorizationHeader);

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no encontrado"
            );
        }

        boolean passwordActualOk;

        try {
            passwordActualOk = passwordEncoder.matches(
                    request.getPasswordActual(),
                    usuario.getPasswordHash()
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña actual es incorrecta"
            );
        }

        if (!passwordActualOk) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña actual es incorrecta"
            );
        }

        validarPasswordNueva(request.getPasswordNueva());

        usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);

        auditLogService.registrar(
                usuario,
                "Configuración",
                "PASSWORD_CAMBIADA",
                "Usuario",
                usuario.getId(),
                "OK",
                "El usuario modificó su contraseña"
        );

        return new CambiarPasswordResponse("Contraseña modificada correctamente");
    }

    public RestablecerPasswordResponse restablecerPassword(RestablecerPasswordRequest request) {
        if (request == null || request.getUsuario() == null || request.getUsuario().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe informar el usuario"
            );
        }

        Usuario usuario = usuarioRepository.findByNombreUsuario(request.getUsuario().trim())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        String passwordTemporal = generarPasswordTemporal();

        usuario.setPasswordHash(passwordEncoder.encode(passwordTemporal));
        usuarioRepository.save(usuario);

        auditLogService.registrar(
                usuario,
                "Configuración",
                "PASSWORD_RESETEADA",
                "Usuario",
                usuario.getId(),
                "OK",
                "Se restableció la contraseña del usuario"
        );

        return new RestablecerPasswordResponse(
                "Contraseña restablecida correctamente",
                passwordTemporal
        );
    }

    private void validarLoginRequest(LoginRequest request) {
        if (request == null ||
                request.getUsuario() == null ||
                request.getUsuario().isBlank() ||
                request.getPassword() == null ||
                request.getPassword().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas"
            );
        }
    }

    private void validarPasswordNueva(String passwordNueva) {
        if (passwordNueva == null || passwordNueva.isBlank() || passwordNueva.length() < 6) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nueva contraseña debe tener al menos 6 caracteres"
            );
        }
    }

    private Usuario resolverUsuarioActual(
            CambiarPasswordRequest request,
            String authorizationHeader
    ) {
        /*
         * Compatibilidad con el front:
         * - El request actual manda passwordActual/passwordNueva.
         * - Si además viniera usuario, lo usamos.
         * - Si no viene usuario, resolvemos desde Authorization Bearer <jwt>.
         */
        if (request.getUsuario() != null && !request.getUsuario().isBlank()) {
            return usuarioRepository.findByNombreUsuario(request.getUsuario().trim())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Usuario no encontrado"
                    ));
        }

        return jwtService.obtenerUsuarioDesdeAuthorization(authorizationHeader);
    }

    public Usuario obtenerUsuarioActual(String authorizationHeader) {
        return jwtService.obtenerUsuarioDesdeAuthorization(authorizationHeader);
    }

    private String generarPasswordTemporal() {
        StringBuilder password = new StringBuilder(PASSWORD_TEMPORAL_PREFIX);

        for (int i = 0; i < 8; i++) {
            int index = SECURE_RANDOM.nextInt(PASSWORD_TEMPORAL_CHARS.length());
            password.append(PASSWORD_TEMPORAL_CHARS.charAt(index));
        }

        return password.toString();
    }
}
