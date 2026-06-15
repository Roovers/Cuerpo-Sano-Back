package com.cuerposano.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "entrenadores",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "dni")
        }
)
public class Entrenador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String dni;

    @Column(name = "especialidad_id", nullable = false)
    private Integer especialidadId;

    @Column(nullable = false)
    private Boolean certificado = false;

    private String telefono;
    private String email;

    @Column(name = "foto_url", columnDefinition = "TEXT")
    private String fotoUrl;

    @Column(nullable = false)
    private Boolean activo = true;

    public Entrenador() {
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setEspecialidadId(Integer especialidadId) {
        this.especialidadId = especialidadId;
    }

    public void setCertificado(Boolean certificado) {
        this.certificado = certificado;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}