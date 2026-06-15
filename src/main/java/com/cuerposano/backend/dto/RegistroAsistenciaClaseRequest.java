package com.cuerposano.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RegistroAsistenciaClaseRequest {

    @NotNull(message = "El horario es obligatorio")
    private Integer horarioId;

    @NotNull(message = "La fecha de clase es obligatoria")
    private LocalDate fechaClase;

    @Valid
    @NotEmpty(message = "Debe informar al menos un registro")
    private List<RegistroAsistenciaClaseItemRequest> registros;
}