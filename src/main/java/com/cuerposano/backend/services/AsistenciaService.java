package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.*;
import com.cuerposano.backend.entities.*;
import com.cuerposano.backend.enums.EstadoAsistenciaClase;
import com.cuerposano.backend.enums.EstadoMembresiaSocio;
import com.cuerposano.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AsistenciaService {

    private final SocioRepository socioRepository;
    private final HorarioRepository horarioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final MembresiaSocioRepository membresiaSocioRepository;
    private final InscripcionClaseRepository inscripcionClaseRepository;
    private final AsistenciaSocioRepository asistenciaSocioRepository;
    private final AsistenciaClaseRepository asistenciaClaseRepository;
    private final AsistenciaEntrenadorRepository asistenciaEntrenadorRepository;

    public AsistenciaService(
            SocioRepository socioRepository,
            HorarioRepository horarioRepository,
            EntrenadorRepository entrenadorRepository,
            MembresiaSocioRepository membresiaSocioRepository,
            InscripcionClaseRepository inscripcionClaseRepository,
            AsistenciaSocioRepository asistenciaSocioRepository,
            AsistenciaClaseRepository asistenciaClaseRepository,
            AsistenciaEntrenadorRepository asistenciaEntrenadorRepository
    ) {
        this.socioRepository = socioRepository;
        this.horarioRepository = horarioRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.membresiaSocioRepository = membresiaSocioRepository;
        this.inscripcionClaseRepository = inscripcionClaseRepository;
        this.asistenciaSocioRepository = asistenciaSocioRepository;
        this.asistenciaClaseRepository = asistenciaClaseRepository;
        this.asistenciaEntrenadorRepository = asistenciaEntrenadorRepository;
    }

    @Transactional
    public AsistenciaSocioResponse registrarAsistenciaSocio(AsistenciaSocioRequest request) {
        Socio socio = buscarSocio(request.getSocioId());

        validarSocioActivo(socio);
        validarMembresiaActiva(socio);

        AsistenciaSocio asistencia = new AsistenciaSocio();
        asistencia.setSocio(socio);
        asistencia.setFechaHora(LocalDateTime.now());
        asistencia.setMetodoIngreso(request.getMetodoIngreso());

        AsistenciaSocio guardada = asistenciaSocioRepository.save(asistencia);
        return toAsistenciaSocioResponse(guardada);
    }

    @Transactional
    public AsistenciaClaseResponse registrarAsistenciaClase(AsistenciaClaseRequest request) {
        Socio socio = buscarSocio(request.getSocioId());
        Horario horario = buscarHorario(request.getHorarioId());
        LocalDate fechaClase = LocalDate.now();

        validarSocioActivo(socio);
        validarMembresiaActiva(socio);
        validarHorarioActivoYVigente(horario, fechaClase);
        validarInscripcionActiva(socio, horario);

        AsistenciaClase asistencia = asistenciaClaseRepository
                .findBySocioIdAndHorarioIdAndFechaClase(socio.getId(), horario.getId(), fechaClase)
                .orElseGet(AsistenciaClase::new);

        asistencia.setSocio(socio);
        asistencia.setHorario(horario);
        asistencia.setFechaClase(fechaClase);
        asistencia.setFechaHora(LocalDateTime.now());
        asistencia.setEstado(EstadoAsistenciaClase.PRESENTE);

        AsistenciaClase guardada = asistenciaClaseRepository.save(asistencia);
        return toAsistenciaClaseResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<AsistenciaClaseResponse> listarAsistenciasClase(
            LocalDateTime desde,
            LocalDateTime hasta
    ) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new RuntimeException("La fecha hasta no puede ser menor a la fecha desde");
        }

        return asistenciaClaseRepository
                .filtrarPorFechaHora(desde, hasta)
                .stream()
                .map(this::toAsistenciaClaseResponse)
                .toList();
    }

    @Transactional
    public RegistroAsistenciaClaseResponse guardarRegistroClase(RegistroAsistenciaClaseRequest request) {
        Horario horario = buscarHorario(request.getHorarioId());

        validarHorarioActivoYVigente(horario, request.getFechaClase());

        int presentes = 0;
        int ausentes = 0;

        for (RegistroAsistenciaClaseItemRequest item : request.getRegistros()) {
            Socio socio = buscarSocio(item.getSocioId());

            validarSocioActivo(socio);
            validarInscripcionActiva(socio, horario);

            EstadoAsistenciaClase estado = parseEstadoAsistencia(item.getEstado());

            AsistenciaClase asistencia = asistenciaClaseRepository
                    .findBySocioIdAndHorarioIdAndFechaClase(
                            socio.getId(),
                            horario.getId(),
                            request.getFechaClase()
                    )
                    .orElseGet(AsistenciaClase::new);

            asistencia.setSocio(socio);
            asistencia.setHorario(horario);
            asistencia.setFechaClase(request.getFechaClase());
            asistencia.setFechaHora(LocalDateTime.now());
            asistencia.setEstado(estado);

            asistenciaClaseRepository.save(asistencia);

            if (estado == EstadoAsistenciaClase.PRESENTE) {
                presentes++;
            } else {
                ausentes++;
            }
        }

        return new RegistroAsistenciaClaseResponse(
                horario.getId(),
                request.getFechaClase(),
                presentes,
                ausentes,
                presentes + ausentes
        );
    }

    @Transactional
    public AsistenciaEntrenadorResponse registrarAsistenciaEntrenador(AsistenciaEntrenadorRequest request) {
        Entrenador entrenador = entrenadorRepository.findById(request.getEntrenadorId())
                .orElseThrow(() -> new RuntimeException("El entrenador no existe"));

        if (!Boolean.TRUE.equals(entrenador.getActivo())) {
            throw new RuntimeException("El entrenador no está activo");
        }

        AsistenciaEntrenador asistencia = new AsistenciaEntrenador();
        asistencia.setEntrenador(entrenador);
        asistencia.setFechaHora(LocalDateTime.now());
        asistencia.setObservacion(request.getObservacion());

        AsistenciaEntrenador guardada = asistenciaEntrenadorRepository.save(asistencia);
        return toAsistenciaEntrenadorResponse(guardada);
    }

    private Socio buscarSocio(Integer socioId) {
        return socioRepository.findById(socioId)
                .orElseThrow(() -> new RuntimeException("El socio no existe"));
    }

    private Horario buscarHorario(Integer horarioId) {
        return horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("El horario no existe"));
    }

    private void validarSocioActivo(Socio socio) {
        if (!Boolean.TRUE.equals(socio.getActivo())) {
            throw new RuntimeException("El socio no está activo");
        }
    }

    private void validarMembresiaActiva(Socio socio) {
        boolean tieneMembresiaActiva = membresiaSocioRepository.findBySocioId(socio.getId())
                .stream()
                .anyMatch(membresia ->
                        membresia.getEstado() == EstadoMembresiaSocio.ACTIVA &&
                                !membresia.getFechaFin().isBefore(LocalDate.now())
                );

        if (!tieneMembresiaActiva) {
            throw new RuntimeException("El socio no tiene una membresía activa");
        }
    }

    private void validarInscripcionActiva(Socio socio, Horario horario) {
        boolean tieneInscripcionActiva = inscripcionClaseRepository
                .existsBySocioIdAndHorarioIdAndActivaTrue(
                        socio.getId(),
                        horario.getId()
                );

        if (!tieneInscripcionActiva) {
            throw new RuntimeException("El socio no está inscripto a esta clase");
        }
    }

    private void validarHorarioActivoYVigente(Horario horario, LocalDate fechaClase) {
        if (!Boolean.TRUE.equals(horario.getActivo())) {
            throw new RuntimeException("El horario no está activo");
        }

        if (fechaClase.isBefore(horario.getFechaDesde()) || fechaClase.isAfter(horario.getFechaHasta())) {
            throw new RuntimeException("La fecha de clase no está dentro de la vigencia del horario");
        }

        if (!horario.getDiaSemana().equals(fechaClase.getDayOfWeek().getValue() % 7)) {
            throw new RuntimeException("La fecha de clase no corresponde al día de la semana del horario");
        }
    }

    private EstadoAsistenciaClase parseEstadoAsistencia(String estado) {
        if (estado == null) {
            throw new RuntimeException("El estado es obligatorio");
        }

        return switch (estado.trim().toUpperCase()) {
            case "PRESENTE" -> EstadoAsistenciaClase.PRESENTE;
            case "AUSENTE" -> EstadoAsistenciaClase.AUSENTE;
            default -> throw new RuntimeException("Estado inválido. Los valores permitidos son Presente o Ausente");
        };
    }

    private String formatearEstado(EstadoAsistenciaClase estado) {
        return switch (estado) {
            case PRESENTE -> "Presente";
            case AUSENTE -> "Ausente";
        };
    }

    private AsistenciaSocioResponse toAsistenciaSocioResponse(AsistenciaSocio asistencia) {
        Socio socio = asistencia.getSocio();

        return new AsistenciaSocioResponse(
                asistencia.getId(),
                socio.getId(),
                socio.getNombre() + " " + socio.getApellido(),
                asistencia.getFechaHora(),
                asistencia.getMetodoIngreso()
        );
    }

    private AsistenciaClaseResponse toAsistenciaClaseResponse(AsistenciaClase asistencia) {
        Socio socio = asistencia.getSocio();
        Horario horario = asistencia.getHorario();
        Entrenador entrenador = horario.getEntrenador();

        return new AsistenciaClaseResponse(
                asistencia.getId(),
                socio.getId(),
                socio.getNombre() + " " + socio.getApellido(),
                horario.getId(),
                horario.getActividad().getId(),
                horario.getActividad().getNombre(),
                entrenador.getId(),
                entrenador.getNombre() + " " + entrenador.getApellido(),
                asistencia.getFechaClase(),
                asistencia.getFechaHora(),
                formatearEstado(asistencia.getEstado())
        );
    }

    private AsistenciaEntrenadorResponse toAsistenciaEntrenadorResponse(AsistenciaEntrenador asistencia) {
        Entrenador entrenador = asistencia.getEntrenador();

        return new AsistenciaEntrenadorResponse(
                asistencia.getId(),
                entrenador.getId(),
                entrenador.getNombre() + " " + entrenador.getApellido(),
                asistencia.getFechaHora(),
                asistencia.getObservacion()
        );
    }
}
