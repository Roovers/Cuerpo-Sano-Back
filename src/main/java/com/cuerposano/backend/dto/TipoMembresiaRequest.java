package com.cuerposano.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TipoMembresiaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "La duración en días es obligatoria")
    @Min(value = 1, message = "La duración debe ser mayor a 0")
    private Integer duracionDias;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    private String descripcion;

    @NotNull(message = "El estado activa es obligatorio")
    private Boolean activa;
}