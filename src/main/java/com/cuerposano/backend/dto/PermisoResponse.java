package com.cuerposano.backend.dto;

public class PermisoResponse {

    private String codigo;
    private String nombre;
    private String descripcion;
    private String categoria;

    public PermisoResponse(String codigo, String nombre, String descripcion, String categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
}
