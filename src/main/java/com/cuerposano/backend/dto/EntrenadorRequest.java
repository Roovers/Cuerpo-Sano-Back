package com.cuerposano.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntrenadorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotNull(message = "La especialidad es obligatoria")
    private Integer especialidadId;

    @NotNull(message = "El certificado es obligatorio")
    private Boolean certificado;

    private String telefono;
    private String email;
    private String fotoBase64;
    private String certificadoBase64;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}