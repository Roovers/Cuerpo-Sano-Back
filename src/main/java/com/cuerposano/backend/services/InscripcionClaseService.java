package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.InscripcionClaseRequest;
import com.cuerposano.backend.dto.InscripcionClaseResponse;
import com.cuerposano.backend.entities.Horario;
import com.cuerposano.backend.entities.InscripcionClase;
import com.cuerposano.backend.entities.MembresiaSocio;
import com.cuerposano.backend.entities.Socio;
import com.cuerposano.backend.enums.EstadoMembresiaSocio;
import com.cuerposano.backend.repositories.HorarioRepository;
import com.cuerposano.backend.repositories.InscripcionClaseRepository;
import com.cuerposano.backend.repositories.MembresiaSocioRepository;
import com.cuerposano.backend.repositories.SocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscripcionClaseService {

    private final InscripcionClaseRepository inscripcionClaseRepository;
    private final SocioRepository socioRepository;
    private final HorarioRepository horarioRepository;
    private final MembresiaSocioRepository membresiaSocioRepository;

    public InscripcionClaseService(
            InscripcionClaseRepository inscripcionClaseRepository,
            SocioRepository socioRepository,
            HorarioRepository horarioRepository,
            MembresiaSocioRepository membresiaSocioRepository
    ) {
        this.inscripcionClaseRepository = inscripcionClaseRepository;
        this.socioRepository = socioRepository;
        this.horarioRepository = horarioRepository;
        this.membresiaSocioRepository = membresiaSocioRepository;
    }

    @Transactional(readOnly = true)
    public List<InscripcionClaseResponse> listar(
            Integer socioId,
            Integer horarioId,
            Boolean activa
    ) {
        return inscripcionClaseRepository
                .filtrar(socioId, horarioId, activa)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InscripcionClaseResponse crear(InscripcionClaseRequest request) {
        Socio socio = socioRepository.findById(request.getSocioId())
                .orElseThrow(() -> new RuntimeException("El socio no existe"));

        Horario horario = horarioRepository.findById(request.getHorarioId())
                .orElseThrow(() -> new RuntimeException("El horario no existe"));

        validarSocio(socio);
        validarHorario(horario);
        validarMembresiaActiva(socio);
        validarCupoDisponible(horario);

        InscripcionClase inscripcion = inscripcionClaseRepository
                .findBySocioIdAndHorarioId(socio.getId(), horario.getId())
                .orElse(null);

        if (inscripcion != null) {
            if (Boolean.TRUE.equals(inscripcion.getActiva())) {
                throw new RuntimeException("El socio ya está inscripto a esta clase");
            }

            inscripcion.setActiva(true);
            inscripcion.setFechaInscripcion(LocalDateTime.now());
            InscripcionClase reactivada = inscripcionClaseRepository.save(inscripcion);
            return toResponse(reactivada);
        }

        InscripcionClase nueva = new InscripcionClase();
        nueva.setSocio(socio);
        nueva.setHorario(horario);
        nueva.setFechaInscripcion(LocalDateTime.now());
        nueva.setActiva(true);

        InscripcionClase guardada = inscripcionClaseRepository.save(nueva);
        return toResponse(guardada);
    }

    @Transactional
    public void eliminar(Integer id) {
        InscripcionClase inscripcion = inscripcionClaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La inscripción no existe"));

        inscripcion.setActiva(false);
        inscripcionClaseRepository.save(inscripcion);
    }

    private void validarSocio(Socio socio) {
        if (!Boolean.TRUE.equals(socio.getActivo())) {
            throw new RuntimeException("El socio no está activo");
        }
    }

    private void validarHorario(Horario horario) {
        if (!Boolean.TRUE.equals(horario.getActivo())) {
            throw new RuntimeException("El horario no está activo");
        }

        /*
         * Permitimos inscribirse a clases futuras.
         * Solo bloqueamos horarios vencidos.
         */
        if (LocalDate.now().isAfter(horario.getFechaHasta())) {
            throw new RuntimeException("La clase ya finalizó su período de vigencia");
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

    private void validarCupoDisponible(Horario horario) {
        long inscriptosActivos = inscripcionClaseRepository.countByHorarioIdAndActivaTrue(horario.getId());
        Integer cupoMaximo = horario.getActividad().getCupoMaximo();

        if (inscriptosActivos >= cupoMaximo) {
            throw new RuntimeException("La clase no tiene cupo disponible");
        }
    }

    private InscripcionClaseResponse toResponse(InscripcionClase inscripcion) {
        Socio socio = inscripcion.getSocio();
        Horario horario = inscripcion.getHorario();

        String socioNombre = socio.getNombre() + " " + socio.getApellido();
        String entrenadorNombre = horario.getEntrenador().getNombre() + " " + horario.getEntrenador().getApellido();

        return new InscripcionClaseResponse(
                inscripcion.getId(),
                socio.getId(),
                socioNombre,
                horario.getId(),
                horario.getActividad().getId(),
                horario.getActividad().getNombre(),
                horario.getEntrenador().getId(),
                entrenadorNombre,
                horario.getDiaSemana(),
                nombreDia(horario.getDiaSemana()),
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.getFechaDesde(),
                horario.getFechaHasta(),
                inscripcion.getFechaInscripcion(),
                inscripcion.getActiva()
        );
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
