package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.*;
import com.cuerposano.backend.entities.*;
import com.cuerposano.backend.enums.EstadoAsistenciaClase;
import com.cuerposano.backend.enums.EstadoMembresiaSocio;
import com.cuerposano.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DashboardService {

    private final SocioRepository socioRepository;
    private final MembresiaSocioRepository membresiaSocioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final HorarioRepository horarioRepository;
    private final PagoRepository pagoRepository;
    private final AsistenciaSocioRepository asistenciaSocioRepository;
    private final AsistenciaClaseRepository asistenciaClaseRepository;
    private final ActividadRepository actividadRepository;

    public DashboardService(
            SocioRepository socioRepository,
            MembresiaSocioRepository membresiaSocioRepository,
            EntrenadorRepository entrenadorRepository,
            HorarioRepository horarioRepository,
            PagoRepository pagoRepository,
            AsistenciaSocioRepository asistenciaSocioRepository,
            AsistenciaClaseRepository asistenciaClaseRepository,
            ActividadRepository actividadRepository
    ) {
        this.socioRepository = socioRepository;
        this.membresiaSocioRepository = membresiaSocioRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.horarioRepository = horarioRepository;
        this.pagoRepository = pagoRepository;
        this.asistenciaSocioRepository = asistenciaSocioRepository;
        this.asistenciaClaseRepository = asistenciaClaseRepository;
        this.actividadRepository = actividadRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResumenResponse resumen() {
        LocalDate hoy = LocalDate.now();

        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(LocalTime.MAX);

        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = hoy.plusMonths(1).withDayOfMonth(1).atStartOfDay().minusSeconds(1);

        BigDecimal ingresosMes = pagoRepository.findByFechaPagoBetween(inicioMes, finMes)
                .stream()
                .map(Pago::getMonto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pagosDelDia = pagoRepository.findByFechaPagoBetween(inicioDia, finDia).size();

        long asistenciasSociosHoy = asistenciaSocioRepository
                .filtrarPorFechaHora(inicioDia, finDia)
                .size();

        long asistenciasClasesHoy = asistenciaClaseRepository
                .filtrarPorFechaHora(inicioDia, finDia)
                .size();

        long membresiasActivas = membresiaSocioRepository.findByEstado(EstadoMembresiaSocio.ACTIVA).size();
        long membresiasPendientes = membresiaSocioRepository.findByEstado(EstadoMembresiaSocio.PENDIENTE_PAGO).size();
        long membresiasVencidas = membresiaSocioRepository.findByEstado(EstadoMembresiaSocio.VENCIDA).size();

        long membresiasPorVencer = membresiaSocioRepository
                .findByFechaFinBetween(hoy, hoy.plusDays(7))
                .stream()
                .filter(membresia -> membresia.getEstado() == EstadoMembresiaSocio.ACTIVA)
                .count();

        long clasesProgramadasHoy = horarioRepository.findAll()
                .stream()
                .filter(horario -> Boolean.TRUE.equals(horario.getActivo()))
                .filter(horario -> !hoy.isBefore(horario.getFechaDesde()))
                .filter(horario -> !hoy.isAfter(horario.getFechaHasta()))
                .filter(horario -> horario.getDiaSemana().equals(hoy.getDayOfWeek().getValue() % 7))
                .count();

        return new DashboardResumenResponse(
                socioRepository.count(),
                socioRepository.countByActivoTrue(),
                membresiasActivas,
                membresiasPendientes,
                membresiasVencidas,
                membresiasPorVencer,
                entrenadorRepository.countByActivoTrue(),
                horarioRepository.countByActivoTrue(),
                clasesProgramadasHoy,
                actividadRepository.findByActiva(true).size() * 1L,
                pagosDelDia,
                ingresosMes,
                ingresosMes,
                asistenciasSociosHoy,
                asistenciasClasesHoy
        );
    }

    @Transactional(readOnly = true)
    public DashboardActividadRecienteResponse actividadReciente() {
        DashboardActividadItemResponse ultimoPago = pagoRepository.findAll()
                .stream()
                .max(Comparator.comparing(Pago::getFechaPago))
                .map(this::toPagoActividad)
                .orElse(null);

        DashboardActividadItemResponse ultimaAsistenciaSocio = asistenciaSocioRepository.findAll()
                .stream()
                .max(Comparator.comparing(AsistenciaSocio::getFechaHora))
                .map(this::toAsistenciaSocioActividad)
                .orElse(null);

        DashboardActividadItemResponse ultimaAsistenciaClase = asistenciaClaseRepository.findAll()
                .stream()
                .max(Comparator.comparing(AsistenciaClase::getFechaHora))
                .map(this::toAsistenciaClaseActividad)
                .orElse(null);

        DashboardActividadItemResponse ultimaAsistencia = elegirMasReciente(
                ultimaAsistenciaSocio,
                ultimaAsistenciaClase
        );

        DashboardActividadItemResponse ultimaMembresiaActivada = membresiaSocioRepository
                .findByEstado(EstadoMembresiaSocio.ACTIVA)
                .stream()
                .max(Comparator.comparing(MembresiaSocio::getFechaInicio))
                .map(this::toMembresiaActividad)
                .orElse(null);

        DashboardActividadItemResponse ultimoSocio = socioRepository.findAll()
                .stream()
                .max(Comparator.comparing(Socio::getId))
                .map(this::toSocioActividad)
                .orElse(null);

        DashboardActividadItemResponse proximaClase = buscarProximaClase();

        List<DashboardActividadItemResponse> clasesEnEsteMomento = buscarClasesEnEsteMomento();

        return new DashboardActividadRecienteResponse(
                ultimoPago,
                ultimaAsistencia,
                ultimaMembresiaActivada,
                ultimoSocio,
                proximaClase,
                clasesEnEsteMomento
        );
    }

    @Transactional(readOnly = true)
    public DashboardGraficosResponse graficos(LocalDateTime desde, LocalDateTime hasta) {
        LocalDateTime desdeFinal = desde != null ? desde : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime hastaFinal = hasta != null ? hasta : LocalDateTime.now();

        List<DashboardGraficoItemResponse> asistenciasPorDia = asistenciaSocioRepository
                .filtrarPorFechaHora(desdeFinal, hastaFinal)
                .stream()
                .collect(
                        LinkedHashMap<String, Long>::new,
                        (map, asistencia) -> {
                            String dia = asistencia.getFechaHora().toLocalDate().toString();
                            map.put(dia, map.getOrDefault(dia, 0L) + 1);
                        },
                        Map::putAll
                )
                .entrySet()
                .stream()
                .map(entry -> DashboardGraficoItemResponse.fechaCantidad(entry.getKey(), entry.getValue()))
                .toList();

        List<DashboardGraficoItemResponse> recaudacionPorDia = pagoRepository
                .findByFechaPagoBetween(desdeFinal, hastaFinal)
                .stream()
                .collect(
                        LinkedHashMap<String, BigDecimal>::new,
                        (map, pago) -> {
                            String dia = pago.getFechaPago().toLocalDate().toString();
                            BigDecimal acumulado = map.getOrDefault(dia, BigDecimal.ZERO);
                            map.put(dia, acumulado.add(pago.getMonto()));
                        },
                        Map::putAll
                )
                .entrySet()
                .stream()
                .map(entry -> DashboardGraficoItemResponse.fechaMonto(entry.getKey(), entry.getValue()))
                .toList();

        List<DashboardGraficoItemResponse> asistenciasPorActividad = asistenciaClaseRepository
                .filtrarPorFechaHora(desdeFinal, hastaFinal)
                .stream()
                .collect(
                        LinkedHashMap<String, Long>::new,
                        (map, asistencia) -> {
                            String actividad = asistencia.getHorario().getActividad().getNombre();
                            map.put(actividad, map.getOrDefault(actividad, 0L) + 1);
                        },
                        Map::putAll
                )
                .entrySet()
                .stream()
                .map(entry -> DashboardGraficoItemResponse.nombreCantidad(entry.getKey(), entry.getValue()))
                .toList();

        List<DashboardGraficoItemResponse> membresiasPorEstado = membresiaSocioRepository
                .findAll()
                .stream()
                .collect(
                        LinkedHashMap<String, Long>::new,
                        (map, membresia) -> {
                            String estado = formatearEstadoMembresia(membresia.getEstado());
                            map.put(estado, map.getOrDefault(estado, 0L) + 1);
                        },
                        Map::putAll
                )
                .entrySet()
                .stream()
                .map(entry -> DashboardGraficoItemResponse.nombreCantidad(entry.getKey(), entry.getValue()))
                .toList();

        return new DashboardGraficosResponse(
                new DashboardFiltrosResponse(
                        desdeFinal.toString(),
                        hastaFinal.toString()
                ),
                recaudacionPorDia,
                asistenciasPorDia,
                asistenciasPorActividad,
                membresiasPorEstado
        );
    }

    @Transactional(readOnly = true)
    public DashboardVencimientosResponse membresiasPorVencer(Integer dias) {
        int diasFinal = dias != null && dias > 0 ? dias : 7;

        LocalDate hoy = LocalDate.now();
        LocalDate hasta = hoy.plusDays(diasFinal);

        List<DashboardMembresiaPorVencerResponse> membresias = membresiaSocioRepository
                .findByFechaFinBetween(hoy, hasta)
                .stream()
                .filter(membresia -> membresia.getEstado() == EstadoMembresiaSocio.ACTIVA)
                .sorted(Comparator.comparing(MembresiaSocio::getFechaFin))
                .map(this::toMembresiaPorVencerResponse)
                .toList();

        return new DashboardVencimientosResponse(
                diasFinal,
                membresias.size(),
                membresias
        );
    }

    private DashboardActividadItemResponse toPagoActividad(Pago pago) {
        DashboardActividadItemResponse item = new DashboardActividadItemResponse();
        String socio = nombreSocio(pago.getSocio());

        item.setTipo("Pago");
        item.setDescripcion("Pago registrado de $" + pago.getMonto() + " - " + socio);
        item.setSocio(socio);
        item.setFecha(pago.getFechaPago());
        item.setFechaHora(pago.getFechaPago());
        item.setMonto(pago.getMonto());
        item.setNumeroSocio(pago.getSocio().getNumeroSocio());

        return item;
    }

    private DashboardActividadItemResponse toAsistenciaSocioActividad(AsistenciaSocio asistencia) {
        DashboardActividadItemResponse item = new DashboardActividadItemResponse();
        String socio = nombreSocio(asistencia.getSocio());

        item.setTipo("AsistenciaSocio");
        item.setDescripcion("Ingreso de socio: " + socio);
        item.setSocio(socio);
        item.setFecha(asistencia.getFechaHora());
        item.setFechaHora(asistencia.getFechaHora());
        item.setNumeroSocio(asistencia.getSocio().getNumeroSocio());

        return item;
    }

    private DashboardActividadItemResponse toAsistenciaClaseActividad(AsistenciaClase asistencia) {
        DashboardActividadItemResponse item = new DashboardActividadItemResponse();
        String socio = nombreSocio(asistencia.getSocio());

        item.setTipo("AsistenciaClase");
        item.setDescripcion(formatearEstadoAsistencia(asistencia.getEstado()) + " - " + socio +
                " en " + asistencia.getHorario().getActividad().getNombre());
        item.setSocio(socio);
        item.setFecha(asistencia.getFechaHora());
        item.setFechaHora(asistencia.getFechaHora());
        item.setActividad(asistencia.getHorario().getActividad().getNombre());
        item.setEntrenador(nombreEntrenador(asistencia.getHorario().getEntrenador()));
        item.setHorario(asistencia.getHorario().getHoraInicio() + " - " + asistencia.getHorario().getHoraFin());

        return item;
    }

    private DashboardActividadItemResponse toMembresiaActividad(MembresiaSocio membresia) {
        DashboardActividadItemResponse item = new DashboardActividadItemResponse();
        String socio = nombreSocio(membresia.getSocio());

        item.setTipo("Membresia");
        item.setDescripcion("Membresía activada para " + socio);
        item.setSocio(socio);
        item.setFecha(membresia.getFechaInicio().atStartOfDay());
        item.setFechaHora(membresia.getFechaInicio().atStartOfDay());
        item.setFechaInicio(membresia.getFechaInicio());
        item.setFechaFin(membresia.getFechaFin());

        return item;
    }

    private DashboardActividadItemResponse toSocioActividad(Socio socio) {
        DashboardActividadItemResponse item = new DashboardActividadItemResponse();

        item.setTipo("Socio");
        item.setDescripcion("Socio registrado: " + nombreSocio(socio));
        item.setSocio(nombreSocio(socio));
        item.setNumeroSocio(socio.getNumeroSocio());

        return item;
    }

    private DashboardActividadItemResponse buscarProximaClase() {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        return horarioRepository.findAll()
                .stream()
                .filter(horario -> Boolean.TRUE.equals(horario.getActivo()))
                .filter(horario -> !hoy.isBefore(horario.getFechaDesde()))
                .filter(horario -> !hoy.isAfter(horario.getFechaHasta()))
                .filter(horario -> horario.getDiaSemana().equals(hoy.getDayOfWeek().getValue() % 7))
                .filter(horario -> !horario.getHoraInicio().isBefore(ahora))
                .min(Comparator.comparing(Horario::getHoraInicio))
                .map(this::toClaseActividad)
                .orElse(null);
    }

    private List<DashboardActividadItemResponse> buscarClasesEnEsteMomento() {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        return horarioRepository.findAll()
                .stream()
                .filter(horario -> Boolean.TRUE.equals(horario.getActivo()))
                .filter(horario -> !hoy.isBefore(horario.getFechaDesde()))
                .filter(horario -> !hoy.isAfter(horario.getFechaHasta()))
                .filter(horario -> horario.getDiaSemana().equals(hoy.getDayOfWeek().getValue() % 7))
                .filter(horario -> !horario.getHoraInicio().isAfter(ahora))
                .filter(horario -> horario.getHoraFin().isAfter(ahora))
                .map(this::toClaseActividad)
                .toList();
    }

    private DashboardActividadItemResponse toClaseActividad(Horario horario) {
        DashboardActividadItemResponse item = new DashboardActividadItemResponse();

        item.setTipo("Clase");
        item.setDescripcion("Clase de " + horario.getActividad().getNombre());
        item.setActividad(horario.getActividad().getNombre());
        item.setEntrenador(nombreEntrenador(horario.getEntrenador()));
        item.setHorario(horario.getHoraInicio() + " - " + horario.getHoraFin());
        item.setFecha(LocalDate.now().atTime(horario.getHoraInicio()));
        item.setFechaHora(LocalDate.now().atTime(horario.getHoraInicio()));

        return item;
    }

    private DashboardMembresiaPorVencerResponse toMembresiaPorVencerResponse(MembresiaSocio membresia) {
        Socio socio = membresia.getSocio();

        long diasRestantes = ChronoUnit.DAYS.between(
                LocalDate.now(),
                membresia.getFechaFin()
        );

        return new DashboardMembresiaPorVencerResponse(
                membresia.getId(),
                socio.getId(),
                nombreSocio(socio),
                nombreSocio(socio),
                socio.getNombre(),
                socio.getApellido(),
                socio.getTelefono(),
                socio.getTelefono(),
                socio.getEmail(),
                socio.getEmail(),
                socio.getNumeroSocio(),
                membresia.getTipoMembresia().getNombre(),
                membresia.getFechaFin(),
                membresia.getFechaFin(),
                diasRestantes
        );
    }

    private DashboardActividadItemResponse elegirMasReciente(
            DashboardActividadItemResponse primero,
            DashboardActividadItemResponse segundo
    ) {
        if (primero == null) return segundo;
        if (segundo == null) return primero;

        if (primero.getFechaHora() == null) return segundo;
        if (segundo.getFechaHora() == null) return primero;

        return primero.getFechaHora().isAfter(segundo.getFechaHora()) ? primero : segundo;
    }

    private String nombreSocio(Socio socio) {
        return socio.getNombre() + " " + socio.getApellido();
    }

    private String nombreEntrenador(Entrenador entrenador) {
        return entrenador.getNombre() + " " + entrenador.getApellido();
    }

    private String formatearEstadoAsistencia(EstadoAsistenciaClase estado) {
        return switch (estado) {
            case PRESENTE -> "Presente";
            case AUSENTE -> "Ausente";
        };
    }

    private String formatearEstadoMembresia(EstadoMembresiaSocio estado) {
        return switch (estado) {
            case PENDIENTE_PAGO -> "Pendiente de pago";
            case ACTIVA -> "Activa";
            case VENCIDA -> "Vencida";
            case CANCELADA -> "Cancelada";
        };
    }
}
