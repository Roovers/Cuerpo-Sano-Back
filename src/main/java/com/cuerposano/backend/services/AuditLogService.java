package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.AuditLogPageResponse;
import com.cuerposano.backend.dto.AuditLogResponse;
import com.cuerposano.backend.entities.AuditLog;
import com.cuerposano.backend.entities.Usuario;
import com.cuerposano.backend.repositories.AuditLogRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final PermisosService permisosService;

    public AuditLogService(AuditLogRepository auditLogRepository, PermisosService permisosService) {
        this.auditLogRepository = auditLogRepository;
        this.permisosService = permisosService;
    }

    @Transactional
    public void registrar(
            Usuario usuario,
            String modulo,
            String accion,
            String entidad,
            Object entidadId,
            String resultado,
            String detalle
    ) {
        try {
            AuditLog log = new AuditLog();
            log.setFechaHora(LocalDateTime.now());
            log.setUsuarioId(usuario != null ? usuario.getId() : null);
            log.setUsuarioNombre(usuario != null ? usuario.getNombreUsuario() : "Sistema");
            log.setRol(usuario != null ? permisosService.normalizarRol(usuario.getRol()) : "Sistema");
            log.setModulo(limitar(modulo, 80));
            log.setAccion(limitar(accion, 120));
            log.setEntidad(limitar(entidad, 100));
            log.setEntidadId(entidadId != null ? limitar(String.valueOf(entidadId), 80) : null);
            log.setResultado(limitar(resultado != null ? resultado : "OK", 40));
            log.setDetalle(limitar(detalle, 700));
            auditLogRepository.save(log);
        } catch (Exception ex) {
            /*
             * Auditoría no debe romper la operación principal.
             * Si fallara el log, la acción funcional continúa.
             */
            System.err.println("No se pudo registrar auditoría: " + ex.getMessage());
        }
    }

    @Transactional
    public void registrarSistema(
            String usuarioNombre,
            String rol,
            String modulo,
            String accion,
            String entidad,
            Object entidadId,
            String resultado,
            String detalle
    ) {
        try {
            AuditLog log = new AuditLog();
            log.setFechaHora(LocalDateTime.now());
            log.setUsuarioNombre(usuarioNombre != null ? usuarioNombre : "Sistema");
            log.setRol(rol != null ? permisosService.normalizarRol(rol) : "Sistema");
            log.setModulo(limitar(modulo, 80));
            log.setAccion(limitar(accion, 120));
            log.setEntidad(limitar(entidad, 100));
            log.setEntidadId(entidadId != null ? limitar(String.valueOf(entidadId), 80) : null);
            log.setResultado(limitar(resultado != null ? resultado : "OK", 40));
            log.setDetalle(limitar(detalle, 700));
            auditLogRepository.save(log);
        } catch (Exception ex) {
            System.err.println("No se pudo registrar auditoría: " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public AuditLogPageResponse listar(
            String desde,
            String hasta,
            String usuario,
            String modulo,
            String accion,
            String resultado,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        LocalDateTime desdeDt = parseDesde(desde);
        LocalDateTime hastaDt = parseHasta(hasta);

        List<AuditLogResponse> filtrados = auditLogRepository
                .findAll(Sort.by(Sort.Direction.DESC, "fechaHora"))
                .stream()
                .filter(log -> desdeDt == null || !log.getFechaHora().isBefore(desdeDt))
                .filter(log -> hastaDt == null || !log.getFechaHora().isAfter(hastaDt))
                .filter(log -> contiene(log.getUsuarioNombre(), usuario))
                .filter(log -> contiene(log.getModulo(), modulo))
                .filter(log -> contiene(log.getAccion(), accion))
                .filter(log -> contiene(log.getResultado(), resultado))
                .map(this::toResponse)
                .toList();

        int total = filtrados.size();
        int from = Math.min(safePage * safeSize, total);
        int to = Math.min(from + safeSize, total);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);

        return new AuditLogPageResponse(
                filtrados.subList(from, to),
                safePage,
                safeSize,
                total,
                totalPages
        );
    }

    @Transactional(readOnly = true)
    public AuditLogResponse obtenerPorId(Integer id) {
        return auditLogRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log no encontrado"));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getFechaHora(),
                log.getUsuarioId(),
                log.getUsuarioNombre(),
                permisosService.normalizarRol(log.getRol()),
                log.getModulo(),
                log.getAccion(),
                log.getEntidad(),
                log.getEntidadId(),
                log.getResultado(),
                log.getDetalle()
        );
    }

    private boolean contiene(String origen, String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return true;
        }

        String a = origen == null ? "" : origen.toLowerCase(Locale.ROOT);
        String b = filtro.trim().toLowerCase(Locale.ROOT);
        return a.contains(b);
    }

    private LocalDateTime parseDesde(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDate.parse(value.trim()).atStartOfDay();
    }

    private LocalDateTime parseHasta(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDate.parse(value.trim()).atTime(23, 59, 59);
    }

    private String limitar(String value, int max) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
