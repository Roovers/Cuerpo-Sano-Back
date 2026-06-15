package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.ReporteAsistenciaClaseResponse;
import com.cuerposano.backend.dto.ReporteAsistenciaSocioResponse;
import com.cuerposano.backend.entities.AsistenciaClase;
import com.cuerposano.backend.entities.AsistenciaSocio;
import com.cuerposano.backend.entities.Entrenador;
import com.cuerposano.backend.entities.Horario;
import com.cuerposano.backend.entities.Socio;
import com.cuerposano.backend.enums.EstadoAsistenciaClase;
import com.cuerposano.backend.repositories.AsistenciaClaseRepository;
import com.cuerposano.backend.repositories.AsistenciaSocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteService {

    private final AsistenciaSocioRepository asistenciaSocioRepository;
    private final AsistenciaClaseRepository asistenciaClaseRepository;
    private final FotoStorageService fotoStorageService;

    public ReporteService(
            AsistenciaSocioRepository asistenciaSocioRepository,
            AsistenciaClaseRepository asistenciaClaseRepository,
            FotoStorageService fotoStorageService
    ) {
        this.asistenciaSocioRepository = asistenciaSocioRepository;
        this.asistenciaClaseRepository = asistenciaClaseRepository;
        this.fotoStorageService = fotoStorageService;
    }

    @Transactional(readOnly = true)
    public List<ReporteAsistenciaSocioResponse> reporteAsistenciaSocios(
            LocalDateTime desde,
            LocalDateTime hasta
    ) {
        return asistenciaSocioRepository
                .filtrarPorFechaHora(desde, hasta)
                .stream()
                .map(this::toReporteAsistenciaSocioResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteAsistenciaClaseResponse> reporteAsistenciaClases(
            LocalDateTime desde,
            LocalDateTime hasta
    ) {
        return asistenciaClaseRepository
                .filtrarPorFechaHora(desde, hasta)
                .stream()
                .map(this::toReporteAsistenciaClaseResponse)
                .toList();
    }

    private ReporteAsistenciaSocioResponse toReporteAsistenciaSocioResponse(
            AsistenciaSocio asistencia
    ) {
        Socio socio = asistencia.getSocio();
        String socioNombre = socio.getNombre() + " " + socio.getApellido();
        String socioFotoUrl = fotoStorageService.normalizarFotoParaRespuesta(socio.getFotoUrl());

        return new ReporteAsistenciaSocioResponse(
                asistencia.getId(),
                socio.getId(),
                socioNombre,
                socioNombre,
                socio.getNombre(),
                socio.getApellido(),
                socio.getDni(),
                socioFotoUrl,
                socioFotoUrl,
                asistencia.getFechaHora(),
                asistencia.getMetodoIngreso()
        );
    }

    private ReporteAsistenciaClaseResponse toReporteAsistenciaClaseResponse(
            AsistenciaClase asistencia
    ) {
        Socio socio = asistencia.getSocio();
        Horario horario = asistencia.getHorario();
        Entrenador entrenador = horario.getEntrenador();

        String socioNombre = socio.getNombre() + " " + socio.getApellido();
        String entrenadorNombre = entrenador.getNombre() + " " + entrenador.getApellido();
        String socioFotoUrl = fotoStorageService.normalizarFotoParaRespuesta(socio.getFotoUrl());
        String entrenadorFotoUrl = fotoStorageService.normalizarFotoParaRespuesta(entrenador.getFotoUrl());

        return new ReporteAsistenciaClaseResponse(
                asistencia.getId(),
                socio.getId(),
                socioNombre,
                socioNombre,
                socio.getNombre(),
                socio.getApellido(),
                socio.getDni(),
                socioFotoUrl,
                socioFotoUrl,
                horario.getId(),
                horario.getActividad().getId(),
                horario.getActividad().getNombre(),
                horario.getActividad().getNombre(),
                entrenador.getId(),
                entrenadorNombre,
                entrenadorNombre,
                entrenadorFotoUrl,
                asistencia.getFechaClase(),
                asistencia.getFechaHora(),
                nombreDia(horario.getDiaSemana()),
                horario.getHoraInicio().toString(),
                horario.getHoraFin().toString(),
                formatearEstado(asistencia.getEstado())
        );
    }

    private String formatearEstado(EstadoAsistenciaClase estado) {
        return switch (estado) {
            case PRESENTE -> "Presente";
            case AUSENTE -> "Ausente";
        };
    }

    private String nombreDia(Integer diaSemana) {
        return switch (diaSemana) {
            case 0 -> "Domingo";
            case 1 -> "Lunes";
            case 2 -> "Martes";
            case 3 -> "Miércoles";
            case 4 -> "Jueves";
            case 5 -> "Viernes";
            case 6 -> "Sábado";
            default -> "Desconocido";
        };
    }
}
