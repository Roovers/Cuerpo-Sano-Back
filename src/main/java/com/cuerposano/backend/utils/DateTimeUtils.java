package com.cuerposano.backend.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static LocalDateTime parseDesde(String value) {
        LocalDateTime parsed = parseNullable(value);

        if (parsed != null) {
            return parsed;
        }

        return null;
    }

    public static LocalDateTime parseHasta(String value) {
        LocalDateTime parsed = parseNullable(value);

        if (parsed != null) {
            return parsed;
        }

        return null;
    }

    public static LocalDateTime parseNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String clean = value.trim();

        try {
            return LocalDateTime.parse(clean);
        } catch (DateTimeParseException ignored) {
            // seguimos probando otros formatos
        }

        try {
            return OffsetDateTime.parse(clean).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // seguimos probando otros formatos
        }

        try {
            return LocalDate.parse(clean).atStartOfDay();
        } catch (DateTimeParseException ignored) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de fecha inválido. Usá yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss"
            );
        }
    }

    public static void validarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha hasta no puede ser menor a la fecha desde"
            );
        }
    }
}
