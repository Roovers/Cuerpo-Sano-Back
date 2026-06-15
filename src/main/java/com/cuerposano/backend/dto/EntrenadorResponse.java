package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EntrenadorResponse {

    private Integer id;
    private String nombre;
    private String apellido;
    private String dni;
    private Integer especialidadId;
    private String especialidadNombre;
    private Boolean certificado;
    private String telefono;
    private String email;
    private String fotoUrl;
    private Boolean activo;
}