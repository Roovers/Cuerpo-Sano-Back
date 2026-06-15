package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.ComprobanteRequest;
import com.cuerposano.backend.dto.ComprobanteResponse;
import com.cuerposano.backend.dto.PagoRequest;
import com.cuerposano.backend.dto.PagoResponse;
import com.cuerposano.backend.dto.PagoSocioResponse;
import com.cuerposano.backend.entities.Comprobante;
import com.cuerposano.backend.entities.Pago;
import com.cuerposano.backend.entities.Socio;
import com.cuerposano.backend.enums.MedioPago;
import com.cuerposano.backend.repositories.ComprobanteRepository;
import com.cuerposano.backend.repositories.PagoRepository;
import com.cuerposano.backend.repositories.SocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final SocioRepository socioRepository;
    private final ComprobanteRepository comprobanteRepository;

    public PagoService(
            PagoRepository pagoRepository,
            SocioRepository socioRepository,
            ComprobanteRepository comprobanteRepository
    ) {
        this.pagoRepository = pagoRepository;
        this.socioRepository = socioRepository;
        this.comprobanteRepository = comprobanteRepository;
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listar(Integer socioId) {
        List<Pago> pagos = socioId != null
                ? pagoRepository.findBySocioId(socioId)
                : pagoRepository.findAll();

        return pagos.stream()
                .sorted(Comparator.comparing(Pago::getFechaPago).reversed())
                .map(this::toPagoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagoResponse obtenerPorId(Integer id) {
        return toPagoResponse(buscarPagoPorId(id));
    }

    @Transactional
    public PagoResponse crear(PagoRequest request) {
        Socio socio = socioRepository.findById(request.getSocioId())
                .orElseThrow(() -> new RuntimeException("El socio no existe"));

        if (!Boolean.TRUE.equals(socio.getActivo())) {
            throw new RuntimeException("El socio no está activo");
        }

        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El pago no posee un monto válido");
        }

        Pago pago = new Pago();
        pago.setSocio(socio);
        pago.setMonto(request.getMonto());
        pago.setMedioPago(mapearMedioPago(request.getMedioPago()));
        pago.setFechaPago(LocalDateTime.now());
        pago.setObservacion(normalizarTexto(request.getObservacion()));

        return toPagoResponse(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponse actualizar(Integer id, PagoRequest request) {
        Pago pago = buscarPagoPorId(id);

        Socio socio = socioRepository.findById(request.getSocioId())
                .orElseThrow(() -> new RuntimeException("El socio no existe"));

        if (!Boolean.TRUE.equals(socio.getActivo())) {
            throw new RuntimeException("El socio no está activo");
        }

        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El pago no posee un monto válido");
        }

        pago.setSocio(socio);
        pago.setMonto(request.getMonto());
        pago.setMedioPago(mapearMedioPago(request.getMedioPago()));
        pago.setObservacion(normalizarTexto(request.getObservacion()));

        return toPagoResponse(pagoRepository.save(pago));
    }

    @Transactional
    public void eliminar(Integer id) {
        Pago pago = buscarPagoPorId(id);

        if (comprobanteRepository.existsByPagoId(id)) {
            throw new RuntimeException("No se puede eliminar el pago porque tiene comprobante asociado");
        }

        pagoRepository.delete(pago);
    }

    @Transactional
    public ComprobanteResponse crearComprobante(Integer pagoId, ComprobanteRequest request) {
        Pago pago = buscarPagoPorId(pagoId);

        if (request != null && request.getPagoId() != null && !request.getPagoId().equals(pagoId)) {
            throw new RuntimeException("El pago informado no coincide con el pago de la URL");
        }

        if (pago.getMonto() == null || pago.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El pago no posee un monto válido");
        }

        if (comprobanteRepository.existsByPagoId(pagoId)) {
            throw new RuntimeException("El pago ya tiene un comprobante generado");
        }

        Comprobante comprobante = new Comprobante();
        comprobante.setPago(pago);
        comprobante.setNumero(generarNumeroComprobante());
        comprobante.setFechaEmision(LocalDateTime.now());
        comprobante.setDetalle(resolverDetalleComprobante(request, pago));

        return toComprobanteResponse(comprobanteRepository.save(comprobante));
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> historial(Integer socioId, LocalDateTime desde, LocalDateTime hasta) {
        return pagoRepository.findAll()
                .stream()
                .filter(pago -> socioId == null || pago.getSocio().getId().equals(socioId))
                .filter(pago -> desde == null || !pago.getFechaPago().isBefore(desde))
                .filter(pago -> hasta == null || !pago.getFechaPago().isAfter(hasta))
                .sorted(Comparator.comparing(Pago::getFechaPago).reversed())
                .map(this::toPagoResponse)
                .toList();
    }

    private Pago buscarPagoPorId(Integer id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el pago con id: " + id));
    }

    private MedioPago mapearMedioPago(Integer codigo) {
        if (codigo == null) {
            throw new RuntimeException("Medio de pago inválido");
        }

        return switch (codigo) {
            case 1 -> MedioPago.EFECTIVO;
            case 2 -> MedioPago.DEBITO;
            case 3 -> MedioPago.CREDITO;
            case 4 -> MedioPago.TRANSFERENCIA;
            case 5 -> MedioPago.QR;
            default -> throw new RuntimeException("Medio de pago inválido");
        };
    }

    private Integer mapearCodigoMedioPago(MedioPago medioPago) {
        return switch (medioPago) {
            case EFECTIVO -> 1;
            case DEBITO -> 2;
            case CREDITO -> 3;
            case TRANSFERENCIA -> 4;
            case QR -> 5;
        };
    }

    private String formatearMedioPago(MedioPago medioPago) {
        return switch (medioPago) {
            case EFECTIVO -> "Efectivo";
            case DEBITO -> "Débito";
            case CREDITO -> "Crédito";
            case TRANSFERENCIA -> "Transferencia";
            case QR -> "QR";
        };
    }

    private PagoResponse toPagoResponse(Pago pago) {
        String socioNombre = pago.getSocio().getNombre() + " " + pago.getSocio().getApellido();

        Comprobante comprobante = comprobanteRepository
                .findFirstByPagoIdOrderByFechaEmisionDesc(pago.getId())
                .orElse(null);

        return new PagoResponse(
                pago.getId(),
                pago.getSocio().getId(),
                socioNombre,
                pago.getMonto(),
                pago.getFechaPago(),
                mapearCodigoMedioPago(pago.getMedioPago()),
                formatearMedioPago(pago.getMedioPago()),
                pago.getObservacion(),
                toPagoSocioResponse(pago.getSocio()),
                comprobante != null,
                comprobante != null ? comprobante.getId() : null,
                comprobante != null ? comprobante.getNumero() : null
        );
    }

    private ComprobanteResponse toComprobanteResponse(Comprobante comprobante) {
        Pago pago = comprobante.getPago();
        PagoResponse pagoResponse = toPagoResponse(pago);
        PagoSocioResponse socioResponse = toPagoSocioResponse(pago.getSocio());

        return new ComprobanteResponse(
                comprobante.getId(),
                pago.getId(),
                comprobante.getNumero(),
                comprobante.getFechaEmision(),
                comprobante.getDetalle(),
                pagoResponse,
                socioResponse,
                pago.getMonto(),
                pago.getFechaPago(),
                mapearCodigoMedioPago(pago.getMedioPago()),
                formatearMedioPago(pago.getMedioPago())
        );
    }

    private PagoSocioResponse toPagoSocioResponse(Socio socio) {
        if (socio == null) {
            return null;
        }

        return new PagoSocioResponse(
                socio.getId(),
                socio.getNumeroSocio(),
                socio.getNombre(),
                socio.getApellido(),
                socio.getDni(),
                socio.getEmail()
        );
    }

    private String resolverDetalleComprobante(ComprobanteRequest request, Pago pago) {
        String observacionPago = normalizarTexto(pago.getObservacion());
        String detalleRequest = request != null ? normalizarTexto(request.getDetalle()) : null;

        /*
         * El front actual envía un texto genérico: "Comprobante del registro N.º X".
         * Para la factura premium conviene mostrar el producto/plan abonado.
         * Por eso, si el pago tiene observación, la usamos como detalle principal.
         */
        if (observacionPago != null && !observacionPago.isBlank()) {
            return observacionPago;
        }

        if (detalleRequest != null &&
                !detalleRequest.isBlank() &&
                !esDetalleGenerico(detalleRequest)) {
            return detalleRequest;
        }

        return "Pago registrado para " +
                pago.getSocio().getNombre() +
                " " +
                pago.getSocio().getApellido();
    }

    private boolean esDetalleGenerico(String detalle) {
        String normalizado = Normalizer
                .normalize(detalle, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();

        return normalizado.contains("comprobante del registro") ||
                normalizado.contains("comprobante de pago");
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }

        String texto = valor.trim();

        return texto.isBlank() ? null : texto;
    }

    private String generarNumeroComprobante() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "C-" + timestamp;
    }
}
