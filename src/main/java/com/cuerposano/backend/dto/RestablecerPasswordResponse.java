package com.cuerposano.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RestablecerPasswordResponse {

    private String mensaje;

    private String passwordTemporal;
}
