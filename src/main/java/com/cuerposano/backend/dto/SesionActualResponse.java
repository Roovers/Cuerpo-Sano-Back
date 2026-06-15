package com.cuerposano.backend.dto;

import java.util.List;

public class SesionActualResponse {

    private Integer id;
    private String nombreUsuario;
    private String rol;
    private Boolean activo;
    private Integer entrenadorId;
    private String entrenadorNombre;
    private List<String> paginas;
    private List<String> acciones;
    private List<String> cards;

    public SesionActualResponse(
            Integer id,
            String nombreUsuario,
            String rol,
            Boolean activo,
            Integer entrenadorId,
            String entrenadorNombre,
            List<String> paginas,
            List<String> acciones,
            List<String> cards
    ) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.activo = activo;
        this.entrenadorId = entrenadorId;
        this.entrenadorNombre = entrenadorNombre;
        this.paginas = paginas;
        this.acciones = acciones;
        this.cards = cards;
    }

    public Integer getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getRol() { return rol; }
    public Boolean getActivo() { return activo; }
    public Integer getEntrenadorId() { return entrenadorId; }
    public String getEntrenadorNombre() { return entrenadorNombre; }
    public List<String> getPaginas() { return paginas; }
    public List<String> getAcciones() { return acciones; }
    public List<String> getCards() { return cards; }
}
