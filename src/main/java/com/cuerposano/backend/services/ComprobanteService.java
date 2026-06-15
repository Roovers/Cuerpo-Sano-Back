package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.ComprobanteRequest;
import com.cuerposano.backend.dto.ComprobanteResponse;
import com.cuerposano.backend.dto.PagoResponse;
import com.cuerposano.backend.dto.PagoSocioResponse;
import com.cuerposano.backend.entities.Comprobante;
import com.cuerposano.backend.entities.Pago;
import com.cuerposano.backend.entities.Socio;
import com.cuerposano.backend.enums.MedioPago;
import com.cuerposano.backend.repositories.ComprobanteRepository;
import com.cuerposano.backend.repositories.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;

@Service
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final PagoRepository pagoRepository;

    public ComprobanteService(
            ComprobanteRepository comprobanteRepository,
            PagoRepository pagoRepository
    ) {
        this.comprobanteRepository = comprobanteRepository;
        this.pagoRepository = pagoRepository;
    }

    @Transactional(readOnly = true)
    public List<ComprobanteResponse> listar(Integer pagoId) {
        List<Comprobante> comprobantes = pagoId != null
                ? comprobanteRepository.findByPagoId(pagoId)
                : comprobanteRepository.findAll();

        return comprobantes.stream()
                .sorted(Comparator.comparing(Comprobante::getFechaEmision).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ComprobanteResponse obtenerPorId(Integer id) {
        return toResponse(buscarComprobantePorId(id));
    }

    @Transactional
    public ComprobanteResponse actualizar(Integer id, ComprobanteRequest request) {
        Comprobante comprobante = buscarComprobantePorId(id);

        Pago pago = pagoRepository.findById(request.getPagoId())
                .orElseThrow(() -> new RuntimeException("El pago no existe"));

        comprobanteRepository
                .findFirstByPagoIdOrderByFechaEmisionDesc(pago.getId())
                .ifPresent(comprobanteExistente -> {
                    if (!comprobanteExistente.getId().equals(id)) {
                        throw new RuntimeException("El pago ya tiene un comprobante generado");
                    }
                });

        comprobante.setPago(pago);
        comprobante.setDetalle(resolverDetalleComprobante(request, pago));

        return toResponse(comprobanteRepository.save(comprobante));
    }

    @Transactional
    public void eliminar(Integer id) {
        Comprobante comprobante = buscarComprobantePorId(id);
        comprobanteRepository.delete(comprobante);
    }

    private Comprobante buscarComprobantePorId(Integer id) {
        return comprobanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el comprobante con id: " + id));
    }

    private ComprobanteResponse toResponse(Comprobante comprobante) {
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

    private PagoResponse toPagoResponse(Pago pago) {
        Comprobante comprobante = comprobanteRepository
                .findFirstByPagoIdOrderByFechaEmisionDesc(pago.getId())
                .orElse(null);

        String socioNombre = pago.getSocio().getNombre() + " " + pago.getSocio().getApellido();

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

    private String resolverDetalleComprobante(ComprobanteRequest request, Pago pago) {
        String observacionPago = normalizarTexto(pago.getObservacion());
        String detalleRequest = request != null ? normalizarTexto(request.getDetalle()) : null;

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
}
