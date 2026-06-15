package com.cuerposano.backend.dto;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Integer id;
    private LocalDateTime fechaHora;
    private Integer usuarioId;
    private String usuarioNombre;
    private String rol;
    private String modulo;
    private String accion;
    private String entidad;
    private String entidadId;
    private String resultado;
    private String detalle;

    public AuditLogResponse(
            Integer id,
            LocalDateTime fechaHora,
            Integer usuarioId,
            String usuarioNombre,
            String rol,
            String modulo,
            String accion,
            String entidad,
            String entidadId,
            String resultado,
            String detalle
    ) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.rol = rol;
        this.modulo = modulo;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.resultado = resultado;
        this.detalle = detalle;
    }

    public Integer getId() { return id; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public Integer getUsuarioId() { return usuarioId; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public String getRol() { return rol; }
    public String getModulo() { return modulo; }
    public String getAccion() { return accion; }
    public String getEntidad() { return entidad; }
    public String getEntidadId() { return entidadId; }
    public String getResultado() { return resultado; }
    public String getDetalle() { return detalle; }
}
