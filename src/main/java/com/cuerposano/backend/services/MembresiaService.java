package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.*;
import com.cuerposano.backend.entities.Comprobante;
import com.cuerposano.backend.entities.MembresiaSocio;
import com.cuerposano.backend.entities.Socio;
import com.cuerposano.backend.entities.TipoMembresia;
import com.cuerposano.backend.enums.EstadoMembresiaSocio;
import com.cuerposano.backend.repositories.ComprobanteRepository;
import com.cuerposano.backend.repositories.MembresiaSocioRepository;
import com.cuerposano.backend.repositories.SocioRepository;
import com.cuerposano.backend.repositories.TipoMembresiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MembresiaService {

    private final TipoMembresiaRepository tipoMembresiaRepository;
    private final MembresiaSocioRepository membresiaSocioRepository;
    private final SocioRepository socioRepository;
    private final ComprobanteRepository comprobanteRepository;

    public MembresiaService(
            TipoMembresiaRepository tipoMembresiaRepository,
            MembresiaSocioRepository membresiaSocioRepository,
            SocioRepository socioRepository,
            ComprobanteRepository comprobanteRepository
    ) {
        this.tipoMembresiaRepository = tipoMembresiaRepository;
        this.membresiaSocioRepository = membresiaSocioRepository;
        this.socioRepository = socioRepository;
        this.comprobanteRepository = comprobanteRepository;
    }

    @Transactional(readOnly = true)
    public List<TipoMembresiaResponse> listarTipos() {
        return tipoMembresiaRepository.findAll()
                .stream()
                .map(this::toTipoResponse)
                .toList();
    }

    @Transactional
    public TipoMembresiaResponse crearTipo(TipoMembresiaRequest request) {
        TipoMembresia tipo = new TipoMembresia();
        tipo.setNombre(request.getNombre());
        tipo.setDuracionDias(request.getDuracionDias());
        tipo.setPrecio(request.getPrecio());
        tipo.setDescripcion(request.getDescripcion());
        tipo.setActiva(request.getActiva());

        TipoMembresia guardado = tipoMembresiaRepository.save(tipo);
        return toTipoResponse(guardado);
    }

    @Transactional
    public TipoMembresiaResponse actualizarTipo(Integer id, TipoMembresiaRequest request) {
        TipoMembresia tipo = buscarTipoPorId(id);

        tipo.setNombre(request.getNombre());
        tipo.setDuracionDias(request.getDuracionDias());
        tipo.setPrecio(request.getPrecio());
        tipo.setDescripcion(request.getDescripcion());
        tipo.setActiva(request.getActiva());

        TipoMembresia actualizado = tipoMembresiaRepository.save(tipo);
        return toTipoResponse(actualizado);
    }

    @Transactional
    public void eliminarTipo(Integer id) {
        TipoMembresia tipo = buscarTipoPorId(id);

        if (membresiaSocioRepository.existsByTipoMembresiaId(id)) {
            throw new RuntimeException("No se puede eliminar: el tipo de membresía está en uso");
        }

        tipoMembresiaRepository.delete(tipo);
    }

    @Transactional(readOnly = true)
    public List<MembresiaSocioResponse> historialSocio(Integer socioId) {
        return membresiaSocioRepository.findBySocioId(socioId)
                .stream()
                .map(this::toMembresiaResponse)
                .toList();
    }

    @Transactional
    public MembresiaCreadaResponse crearMembresiaSocio(MembresiaSocioRequest request) {
        Socio socio = socioRepository.findById(request.getSocioId())
                .orElseThrow(() -> new RuntimeException("El socio no existe"));

        if (!Boolean.TRUE.equals(socio.getActivo())) {
            throw new RuntimeException("El socio no está activo");
        }

        TipoMembresia tipo = buscarTipoPorId(request.getTipoMembresiaId());

        if (!Boolean.TRUE.equals(tipo.getActiva())) {
            throw new RuntimeException("El tipo de membresía no está activo");
        }

        boolean tienePendienteOActiva = membresiaSocioRepository.findBySocioId(socio.getId())
                .stream()
                .anyMatch(membresia ->
                        membresia.getEstado() == EstadoMembresiaSocio.PENDIENTE_PAGO ||
                                (
                                        membresia.getEstado() == EstadoMembresiaSocio.ACTIVA &&
                                                !membresia.getFechaFin().isBefore(LocalDate.now())
                                )
                );

        if (tienePendienteOActiva) {
            throw new RuntimeException("El socio ya tiene una membresía pendiente de pago o activa");
        }

        MembresiaSocio membresia = new MembresiaSocio();
        membresia.setSocio(socio);
        membresia.setTipoMembresia(tipo);
        membresia.setFechaInicio(request.getFechaInicio());
        membresia.setFechaFin(request.getFechaInicio().plusDays(tipo.getDuracionDias()));
        membresia.setEstado(EstadoMembresiaSocio.PENDIENTE_PAGO);
        membresia.setPago(null);

        MembresiaSocio guardada = membresiaSocioRepository.save(membresia);

        return new MembresiaCreadaResponse(
                "Membresía creada correctamente. Queda pendiente de pago.",
                toMembresiaResponse(guardada)
        );
    }

    @Transactional
    public MembresiaActivadaResponse activarMembresia(Integer id, String numeroComprobante) {
        MembresiaSocio membresia = membresiaSocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La membresía no existe"));

        if (membresia.getEstado() == EstadoMembresiaSocio.ACTIVA) {
            throw new RuntimeException("La membresía ya está activa");
        }

        if (numeroComprobante == null || numeroComprobante.isBlank()) {
            throw new RuntimeException("El número de comprobante es obligatorio");
        }

        Comprobante comprobante = comprobanteRepository.findByNumero(numeroComprobante)
                .orElseThrow(() -> new RuntimeException("El comprobante no existe"));

        if (comprobante.getPago() == null) {
            throw new RuntimeException("El comprobante no tiene un pago asociado");
        }

        if (!comprobante.getPago().getSocio().getId().equals(membresia.getSocio().getId())) {
            throw new RuntimeException("El comprobante no corresponde al socio de la membresía");
        }

        membresia.setPago(comprobante.getPago());
        membresia.setEstado(EstadoMembresiaSocio.ACTIVA);

        MembresiaSocio actualizada = membresiaSocioRepository.save(membresia);

        return new MembresiaActivadaResponse(
                "Membresía activada correctamente",
                toMembresiaResponse(actualizada),
                comprobante.getNumero()
        );
    }

    @Transactional(readOnly = true)
    public List<MembresiaSocioResponse> avisosVencimiento(Integer dias) {
        int diasFinal = dias != null ? dias : 7;

        LocalDate hoy = LocalDate.now();
        LocalDate hasta = hoy.plusDays(diasFinal);

        return membresiaSocioRepository.findByFechaFinBetween(hoy, hasta)
                .stream()
                .filter(membresia -> membresia.getEstado() == EstadoMembresiaSocio.ACTIVA)
                .map(this::toMembresiaResponse)
                .toList();
    }

    private TipoMembresia buscarTipoPorId(Integer id) {
        return tipoMembresiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el tipo de membresía con id: " + id));
    }

    private TipoMembresiaResponse toTipoResponse(TipoMembresia tipo) {
        return new TipoMembresiaResponse(
                tipo.getId(),
                tipo.getNombre(),
                tipo.getDuracionDias(),
                tipo.getPrecio(),
                tipo.getDescripcion(),
                tipo.getActiva()
        );
    }

    private MembresiaSocioResponse toMembresiaResponse(MembresiaSocio membresia) {
        Integer pagoId = membresia.getPago() != null ? membresia.getPago().getId() : null;

        String socioNombre = membresia.getSocio().getNombre() + " " + membresia.getSocio().getApellido();

        return new MembresiaSocioResponse(
                membresia.getId(),
                membresia.getSocio().getId(),
                socioNombre,
                membresia.getTipoMembresia().getId(),
                membresia.getTipoMembresia().getNombre(),
                membresia.getFechaInicio(),
                membresia.getFechaFin(),
                formatearEstado(membresia.getEstado()),
                pagoId
        );
    }

    private String formatearEstado(EstadoMembresiaSocio estado) {
        return switch (estado) {
            case PENDIENTE_PAGO -> "PendientePago";
            case ACTIVA -> "Activa";
            case VENCIDA -> "Vencida";
            case CANCELADA -> "Cancelada";
        };
    }
}