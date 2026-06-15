package com.cuerposano.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "usuario_nombre", length = 120)
    private String usuarioNombre;

    @Column(length = 80)
    private String rol;

    @Column(nullable = false, length = 80)
    private String modulo;

    @Column(nullable = false, length = 120)
    private String accion;

    @Column(length = 100)
    private String entidad;

    @Column(name = "entidad_id", length = 80)
    private String entidadId;

    @Column(nullable = false, length = 40)
    private String resultado;

    @Column(length = 700)
    private String detalle;

    public AuditLog() {
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public void setEntidadId(String entidadId) {
        this.entidadId = entidadId;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}
