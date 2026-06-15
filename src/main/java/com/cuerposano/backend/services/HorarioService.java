package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.HorarioRequest;
import com.cuerposano.backend.dto.HorarioResponse;
import com.cuerposano.backend.entities.Actividad;
import com.cuerposano.backend.entities.Entrenador;
import com.cuerposano.backend.entities.Horario;
import com.cuerposano.backend.repositories.ActividadRepository;
import com.cuerposano.backend.repositories.EntrenadorRepository;
import com.cuerposano.backend.repositories.HorarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class HorarioService {

    private final HorarioRepository horarioRepository;
    private final ActividadRepository actividadRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final FotoStorageService fotoStorageService;

    public HorarioService(
            HorarioRepository horarioRepository,
            ActividadRepository actividadRepository,
            EntrenadorRepository entrenadorRepository,
            FotoStorageService fotoStorageService
    ) {
        this.horarioRepository = horarioRepository;
        this.actividadRepository = actividadRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.fotoStorageService = fotoStorageService;
    }

    @Transactional(readOnly = true)
    public List<HorarioResponse> listar(
            Integer actividadId,
            Integer entrenadorId,
            Integer dia,
            Boolean activo,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {
        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            throw new RuntimeException("La fecha hasta no puede ser menor a la fecha desde");
        }

        return horarioRepository
                .filtrar(actividadId, entrenadorId, dia, activo, fechaDesde, fechaHasta)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public HorarioResponse crear(HorarioRequest request) {
        validarFechasYHoras(request);

        Actividad actividad = buscarActividad(request.getActividadId());
        Entrenador entrenador = buscarEntrenador(request.getEntrenadorId());

        validarReglasDeNegocio(actividad, entrenador);
        validarSuperposicion(null, request);

        Horario horario = new Horario();
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setActividad(actividad);
        horario.setEntrenador(entrenador);
        horario.setFechaDesde(request.getFechaDesde());
        horario.setFechaHasta(request.getFechaHasta());
        horario.setActivo(request.getActivo());

        Horario guardado = horarioRepository.save(horario);
        return toResponse(guardado);
    }

    @Transactional
    public HorarioResponse actualizar(Integer id, HorarioRequest request) {
        Horario horario = buscarHorarioPorId(id);

        validarFechasYHoras(request);

        Actividad actividad = buscarActividad(request.getActividadId());
        Entrenador entrenador = buscarEntrenador(request.getEntrenadorId());

        validarReglasDeNegocio(actividad, entrenador);
        validarSuperposicion(id, request);

        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setActividad(actividad);
        horario.setEntrenador(entrenador);
        horario.setFechaDesde(request.getFechaDesde());
        horario.setFechaHasta(request.getFechaHasta());
        horario.setActivo(request.getActivo());

        Horario actualizado = horarioRepository.save(horario);
        return toResponse(actualizado);
    }

    @Transactional
    public void eliminar(Integer id) {
        Horario horario = buscarHorarioPorId(id);

        /*
         * Baja lógica para no romper inscripciones/asistencias asociadas.
         * Esto mantiene historial y reportes consistentes.
         */
        horario.setActivo(false);
        horarioRepository.save(horario);
    }

    private Horario buscarHorarioPorId(Integer id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el horario con id: " + id));
    }

    private Actividad buscarActividad(Integer id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la actividad con id: " + id));
    }

    private Entrenador buscarEntrenador(Integer id) {
        return entrenadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el entrenador con id: " + id));
    }

    private void validarFechasYHoras(HorarioRequest request) {
        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new RuntimeException("La hora de fin debe ser posterior a la hora de inicio");
        }

        if (request.getFechaHasta().isBefore(request.getFechaDesde())) {
            throw new RuntimeException("La fecha hasta no puede ser menor a la fecha desde");
        }
    }

    private void validarReglasDeNegocio(Actividad actividad, Entrenador entrenador) {
        if (!Boolean.TRUE.equals(actividad.getActiva())) {
            throw new RuntimeException("La actividad seleccionada no está activa");
        }

        if (!Boolean.TRUE.equals(entrenador.getActivo())) {
            throw new RuntimeException("El entrenador seleccionado no está activo");
        }

        if (!Boolean.TRUE.equals(entrenador.getCertificado())) {
            throw new RuntimeException("El entrenador seleccionado no está certificado");
        }

        if (!entrenador.getEspecialidadId().equals(actividad.getId())) {
            throw new RuntimeException("La especialidad del entrenador no es compatible con la actividad");
        }
    }

    private void validarSuperposicion(Integer idIgnorar, HorarioRequest request) {
        List<Horario> superposiciones = horarioRepository.buscarSuperposiciones(
                idIgnorar,
                request.getDiaSemana(),
                request.getActividadId(),
                request.getEntrenadorId(),
                request.getHoraInicio(),
                request.getHoraFin(),
                request.getFechaDesde(),
                request.getFechaHasta()
        );

        if (!superposiciones.isEmpty()) {
            throw new RuntimeException("Ya existe una clase superpuesta para ese día, horario y rango de fechas");
        }
    }

    private HorarioResponse toResponse(Horario horario) {
        String entrenadorNombre = horario.getEntrenador().getNombre() + " " + horario.getEntrenador().getApellido();

        return new HorarioResponse(
                horario.getId(),
                horario.getDiaSemana(),
                nombreDia(horario.getDiaSemana()),
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.getActividad().getId(),
                horario.getActividad().getNombre(),
                horario.getEntrenador().getId(),
                entrenadorNombre,
                fotoStorageService.normalizarFotoParaRespuesta(horario.getEntrenador().getFotoUrl()),
                horario.getFechaDesde(),
                horario.getFechaHasta(),
                horario.getActivo()
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
