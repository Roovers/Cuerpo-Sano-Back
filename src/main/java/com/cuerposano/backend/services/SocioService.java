package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.SocioCarnetResponse;
import com.cuerposano.backend.dto.SocioRequest;
import com.cuerposano.backend.dto.SocioResponse;
import com.cuerposano.backend.entities.MembresiaSocio;
import com.cuerposano.backend.entities.Socio;
import com.cuerposano.backend.repositories.MembresiaSocioRepository;
import com.cuerposano.backend.repositories.SocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class SocioService {

    private final SocioRepository socioRepository;
    private final MembresiaSocioRepository membresiaSocioRepository;
    private final FotoStorageService fotoStorageService;

    public SocioService(
            SocioRepository socioRepository,
            MembresiaSocioRepository membresiaSocioRepository,
            FotoStorageService fotoStorageService
    ) {
        this.socioRepository = socioRepository;
        this.membresiaSocioRepository = membresiaSocioRepository;
        this.fotoStorageService = fotoStorageService;
    }

    @Transactional(readOnly = true)
    public List<SocioResponse> listar(String buscar) {
        List<Socio> socios;

        if (buscar != null && !buscar.trim().isEmpty()) {
            socios = socioRepository.buscar(buscar.trim());
        } else {
            socios = socioRepository.findAll()
                    .stream()
                    .sorted((a, b) -> a.getApellido().compareToIgnoreCase(b.getApellido()))
                    .toList();
        }

        return socios.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SocioResponse obtenerPorId(Integer id) {
        Socio socio = buscarEntidadPorId(id);
        return toResponse(socio);
    }

    @Transactional
    public SocioResponse crear(SocioRequest request) {
        if (socioRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("DNI duplicado");
        }

        Socio socio = new Socio();
        socio.setNumeroSocio(generarNumeroSocio());
        socio.setCodigoBarra(generarCodigoBarra());
        socio.setNombre(request.getNombre());
        socio.setApellido(request.getApellido());
        socio.setDni(request.getDni());
        socio.setFechaNacimiento(request.getFechaNacimiento());
        socio.setDireccion(request.getDireccion());
        socio.setTelefono(request.getTelefono());
        socio.setEmail(request.getEmail());
        socio.setFotoUrl(procesarFoto(request.getFotoBase64(), "socio_" + request.getDni()));
        socio.setActivo(true);

        Socio guardado = socioRepository.save(socio);
        return toResponse(guardado);
    }

    @Transactional
    public SocioResponse actualizar(Integer id, SocioRequest request) {
        Socio socio = buscarEntidadPorId(id);

        socioRepository.findByDni(request.getDni())
                .ifPresent(socioExistente -> {
                    if (!socioExistente.getId().equals(id)) {
                        throw new RuntimeException("DNI duplicado");
                    }
                });

        socio.setNombre(request.getNombre());
        socio.setApellido(request.getApellido());
        socio.setDni(request.getDni());
        socio.setFechaNacimiento(request.getFechaNacimiento());
        socio.setDireccion(request.getDireccion());
        socio.setTelefono(request.getTelefono());
        socio.setEmail(request.getEmail());

        if (request.getFotoBase64() != null && !request.getFotoBase64().isBlank()) {
            socio.setFotoUrl(procesarFoto(request.getFotoBase64(), "socio_" + request.getDni()));
        }

        Socio actualizado = socioRepository.save(socio);
        return toResponse(actualizado);
    }

    @Transactional
    public void eliminar(Integer id) {
        Socio socio = buscarEntidadPorId(id);
        socio.setActivo(false);
        socioRepository.save(socio);
    }

    @Transactional(readOnly = true)
    public SocioCarnetResponse obtenerCarnet(Integer id) {
        Socio socio = buscarEntidadPorId(id);

        return new SocioCarnetResponse(
                socio.getId(),
                socio.getNumeroSocio(),
                socio.getNombre(),
                socio.getApellido(),
                fotoStorageService.normalizarFotoParaRespuesta(socio.getFotoUrl()),
                socio.getCodigoBarra()
        );
    }

    private Socio buscarEntidadPorId(Integer id) {
        return socioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el socio con id: " + id));
    }

    private SocioResponse toResponse(Socio socio) {
        String estadoMembresia = membresiaSocioRepository
                .findFirstBySocioIdOrderByFechaFinDesc(socio.getId())
                .map(MembresiaSocio::getEstado)
                .map(Enum::name)
                .orElse("SinMembresia");

        return new SocioResponse(
                socio.getId(),
                socio.getNumeroSocio(),
                socio.getCodigoBarra(),
                socio.getNombre(),
                socio.getApellido(),
                socio.getDni(),
                socio.getFechaNacimiento(),
                socio.getDireccion(),
                socio.getTelefono(),
                socio.getEmail(),
                fotoStorageService.normalizarFotoParaRespuesta(socio.getFotoUrl()),
                socio.getActivo(),
                estadoMembresia
        );
    }

    private String generarNumeroSocio() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "S" + timestamp;
    }

    private String generarCodigoBarra() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    private String procesarFoto(String fotoBase64, String prefijoArchivo) {
        return fotoStorageService.guardarFoto(fotoBase64, prefijoArchivo);
    }
}
