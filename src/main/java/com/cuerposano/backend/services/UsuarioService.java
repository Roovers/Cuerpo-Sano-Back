package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.RolUsuarioResponse;
import com.cuerposano.backend.dto.UsuarioRequest;
import com.cuerposano.backend.dto.UsuarioResponse;
import com.cuerposano.backend.dto.UsuarioUpdateRequest;
import com.cuerposano.backend.entities.Entrenador;
import com.cuerposano.backend.entities.Usuario;
import com.cuerposano.backend.repositories.EntrenadorRepository;
import com.cuerposano.backend.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private static final String ROL_ADMINISTRADOR = "Administrador";
    private static final String ROL_RECEPCIONISTA = "Recepcionista";
    private static final String ROL_PROFESOR = "Profesor";

    private final UsuarioRepository usuarioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final PermisosService permisosService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EntrenadorRepository entrenadorRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService,
            PermisosService permisosService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.permisosService = permisosService;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(usuario -> usuario.getNombreUsuario().toLowerCase(Locale.ROOT)))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Integer id) {
        Usuario usuario = buscarEntidadPorId(id);
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        String nombreUsuario = normalizarNombreUsuario(request.getNombreUsuario());
        String rol = normalizarRol(request.getRol());

        validarPasswordInicial(request.getPassword());

        if (usuarioRepository.existsByNombreUsuario(nombreUsuario)) {
            throw new RuntimeException("El usuario ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(request.getActivo());
        usuario.setEntrenador(buscarEntrenadorOpcional(request.getEntrenadorId()));

        Usuario guardado = usuarioRepository.save(usuario);
        auditLogService.registrar(
                guardado,
                "Configuración",
                "USUARIO_CREADO",
                "Usuario",
                guardado.getId(),
                "OK",
                "Usuario creado con rol " + permisosService.normalizarRol(guardado.getRol())
        );
        return toResponse(guardado);
    }

    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioUpdateRequest request) {
        Usuario usuario = buscarEntidadPorId(id);

        String nombreUsuario = normalizarNombreUsuario(request.getNombreUsuario());
        String rolNuevo = normalizarRol(request.getRol());

        usuarioRepository.findByNombreUsuario(nombreUsuario)
                .ifPresent(usuarioExistente -> {
                    if (!usuarioExistente.getId().equals(id)) {
                        throw new RuntimeException("El usuario ya existe");
                    }
                });

        validarNoDejarSistemaSinAdministrador(usuario, rolNuevo, request.getActivo());

        usuario.setNombreUsuario(nombreUsuario);
        usuario.setRol(rolNuevo);
        usuario.setActivo(request.getActivo());
        usuario.setEntrenador(buscarEntrenadorOpcional(request.getEntrenadorId()));

        Usuario actualizado = usuarioRepository.save(usuario);
        auditLogService.registrar(
                actualizado,
                "Configuración",
                "USUARIO_EDITADO",
                "Usuario",
                actualizado.getId(),
                "OK",
                "Usuario actualizado desde configuración"
        );
        return toResponse(actualizado);
    }

    @Transactional
    public void eliminar(Integer id) {
        Usuario usuario = buscarEntidadPorId(id);

        validarNoDejarSistemaSinAdministrador(usuario, usuario.getRol(), false);

        /*
         * Baja lógica igual que el backend de Huilen.
         * No borramos físicamente para no perder consistencia.
         */
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        auditLogService.registrar(
                usuario,
                "Configuración",
                "USUARIO_DESACTIVADO",
                "Usuario",
                usuario.getId(),
                "OK",
                "Usuario desactivado desde configuración"
        );
    }

    @Transactional(readOnly = true)
    public List<RolUsuarioResponse> listarRoles() {
        /*
         * Clon funcional del backend C# de Huilen:
         * el front espera [{ id, nombre }], no un string[].
         */
        return List.of(
                new RolUsuarioResponse(1, ROL_ADMINISTRADOR),
                new RolUsuarioResponse(2, ROL_PROFESOR),
                new RolUsuarioResponse(3, ROL_RECEPCIONISTA)
        );
    }

    private Usuario buscarEntidadPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Entrenador buscarEntrenadorOpcional(Integer entrenadorId) {
        if (entrenadorId == null) {
            return null;
        }

        Entrenador entrenador = entrenadorRepository.findById(entrenadorId)
                .orElseThrow(() -> new RuntimeException("No se encontró el entrenador con id: " + entrenadorId));

        if (!Boolean.TRUE.equals(entrenador.getActivo())) {
            throw new RuntimeException("El entrenador seleccionado no está activo");
        }

        return entrenador;
    }

    private String normalizarNombreUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new RuntimeException("El nombre de usuario es obligatorio");
        }

        return nombreUsuario.trim();
    }

    private void validarPasswordInicial(String password) {
        if (password == null || password.isBlank() || password.length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }
    }

    private String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            throw new RuntimeException("Rol inválido");
        }

        String valor = rol.trim();

        if (valor.equalsIgnoreCase(ROL_ADMINISTRADOR)) {
            return ROL_ADMINISTRADOR;
        }

        if (valor.equalsIgnoreCase(ROL_PROFESOR) || valor.equalsIgnoreCase("Entrenador")) {
            return ROL_PROFESOR;
        }

        if (valor.equalsIgnoreCase(ROL_RECEPCIONISTA)) {
            return ROL_RECEPCIONISTA;
        }

        throw new RuntimeException("Rol inválido");
    }

    private void validarNoDejarSistemaSinAdministrador(
            Usuario usuario,
            String rolNuevo,
            Boolean activoNuevo
    ) {
        boolean eraAdministradorActivo =
                ROL_ADMINISTRADOR.equals(permisosService.normalizarRol(usuario.getRol())) &&
                        Boolean.TRUE.equals(usuario.getActivo());

        boolean seguiraSiendoAdministradorActivo =
                ROL_ADMINISTRADOR.equals(permisosService.normalizarRol(rolNuevo)) &&
                        Boolean.TRUE.equals(activoNuevo);

        if (!eraAdministradorActivo || seguiraSiendoAdministradorActivo) {
            return;
        }

        long administradoresActivos = usuarioRepository.countByRolAndActivoTrue(ROL_ADMINISTRADOR);

        if (administradoresActivos <= 1) {
            throw new RuntimeException("No se puede desactivar o modificar el último administrador activo");
        }
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        Integer entrenadorId = null;
        String entrenadorNombre = null;

        if (usuario.getEntrenador() != null) {
            entrenadorId = usuario.getEntrenador().getId();
            entrenadorNombre = usuario.getEntrenador().getNombre() + " " + usuario.getEntrenador().getApellido();
        }

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreUsuario(),
                permisosService.normalizarRol(usuario.getRol()),
                usuario.getActivo(),
                entrenadorId,
                entrenadorNombre
        );
    }
}
