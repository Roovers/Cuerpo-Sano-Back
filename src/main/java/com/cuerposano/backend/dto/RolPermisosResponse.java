package com.cuerposano.backend.dto;

import java.util.List;

public class RolPermisosResponse {

    private String rol;
    private String descripcion;
    private List<PermisoResponse> paginas;
    private List<PermisoResponse> acciones;
    private List<PermisoResponse> cards;
    private List<String> restricciones;

    public RolPermisosResponse(
            String rol,
            String descripcion,
            List<PermisoResponse> paginas,
            List<PermisoResponse> acciones,
            List<PermisoResponse> cards,
            List<String> restricciones
    ) {
        this.rol = rol;
        this.descripcion = descripcion;
        this.paginas = paginas;
        this.acciones = acciones;
        this.cards = cards;
        this.restricciones = restricciones;
    }

    public String getRol() { return rol; }
    public String getDescripcion() { return descripcion; }
    public List<PermisoResponse> getPaginas() { return paginas; }
    public List<PermisoResponse> getAcciones() { return acciones; }
    public List<PermisoResponse> getCards() { return cards; }
    public List<String> getRestricciones() { return restricciones; }
}
