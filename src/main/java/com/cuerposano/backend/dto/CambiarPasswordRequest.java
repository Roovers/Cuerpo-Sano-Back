package com.cuerposano.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarPasswordRequest {

    /*
     * Campo opcional.
     * El front actual no lo envía, pero lo dejamos para compatibilidad
     * con operaciones administrativas o pruebas desde Swagger.
     */
    private String usuario;

    private String passwordActual;

    private String passwordNueva;
}
