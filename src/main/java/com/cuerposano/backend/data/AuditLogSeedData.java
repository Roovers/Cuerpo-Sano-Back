package com.cuerposano.backend.data;

import com.cuerposano.backend.entities.Usuario;
import com.cuerposano.backend.repositories.AuditLogRepository;
import com.cuerposano.backend.repositories.UsuarioRepository;
import com.cuerposano.backend.services.AuditLogService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class AuditLogSeedData implements CommandLineRunner {

    private final AuditLogRepository auditLogRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditLogService auditLogService;

    public AuditLogSeedData(
            AuditLogRepository auditLogRepository,
            UsuarioRepository usuarioRepository,
            AuditLogService auditLogService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public void run(String... args) {
        if (auditLogRepository.count() > 0) {
            return;
        }

        Usuario admin = usuarioRepository.findByNombreUsuario("admin").orElse(null);
        Usuario recepcion = usuarioRepository.findByNombreUsuario("recepcion").orElse(null);
        Usuario profesor = usuarioRepository.findByNombreUsuario("carlos.lopez").orElse(null);

        auditLogService.registrar(admin, "Configuración", "SEED_INICIAL", "Sistema", null, "OK", "Carga inicial de datos demo realistas");
        auditLogService.registrar(admin, "Usuarios", "USUARIO_CREADO", "Usuario", admin != null ? admin.getId() : null, "OK", "Usuario administrador disponible para demo");
        auditLogService.registrar(recepcion, "Asistencias", "INGRESO_REGISTRADO", "AsistenciaSocio", null, "OK", "Registro demo de ingreso al gimnasio");
        auditLogService.registrar(profesor, "Asistencias", "ASISTENCIA_CLASE_REGISTRADA", "AsistenciaClase", null, "OK", "Registro demo de presentes y ausentes en clase");
        auditLogService.registrar(admin, "Pagos", "PAGO_REGISTRADO", "Pago", null, "OK", "Pago demo registrado con comprobante");
        auditLogService.registrarSistema("usuario.inactivo", "Recepcionista", "Auth", "LOGIN_FALLIDO", "Usuario", null, "ERROR", "Intento de acceso con usuario inactivo");
    }
}
