package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UsuarioResponse {

    private Integer id;

    private String nombreUsuario;

    private String rol;

    private Boolean activo;

    /*
     * Campos extra para la mejora Java:
     * permiten asociar un usuario con un entrenador.
     * Si el front no los usa, no rompen compatibilidad.
     */
    private Integer entrenadorId;

    private String entrenadorNombre;
}
