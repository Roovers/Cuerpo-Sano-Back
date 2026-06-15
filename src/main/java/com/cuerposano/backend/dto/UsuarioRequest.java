package com.cuerposano.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;

    /*
     * Campo opcional agregado por la versión Java.
     * Si el front actual no lo envía, queda null y no rompe compatibilidad.
     */
    private Integer entrenadorId;
}
