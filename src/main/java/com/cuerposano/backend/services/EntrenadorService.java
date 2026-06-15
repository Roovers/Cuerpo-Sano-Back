package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.EntrenadorCertificadoResponse;
import com.cuerposano.backend.dto.EntrenadorRequest;
import com.cuerposano.backend.dto.EntrenadorResponse;
import com.cuerposano.backend.entities.Actividad;
import com.cuerposano.backend.entities.Entrenador;
import com.cuerposano.backend.repositories.ActividadRepository;
import com.cuerposano.backend.repositories.EntrenadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EntrenadorService {

    private final EntrenadorRepository entrenadorRepository;
    private final ActividadRepository actividadRepository;
    private final FotoStorageService fotoStorageService;

    public EntrenadorService(
            EntrenadorRepository entrenadorRepository,
            ActividadRepository actividadRepository,
            FotoStorageService fotoStorageService
    ) {
        this.entrenadorRepository = entrenadorRepository;
        this.actividadRepository = actividadRepository;
        this.fotoStorageService = fotoStorageService;
    }

    @Transactional(readOnly = true)
    public List<EntrenadorResponse> listar(String buscar) {
        List<Entrenador> entrenadores;

        if (buscar != null && !buscar.trim().isEmpty()) {
            entrenadores = entrenadorRepository.buscar(buscar.trim());
        } else {
            entrenadores = entrenadorRepository.findAll()
                    .stream()
                    .sorted((a, b) -> a.getApellido().compareToIgnoreCase(b.getApellido()))
                    .toList();
        }

        return entrenadores.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EntrenadorResponse obtenerPorId(Integer id) {
        Entrenador entrenador = buscarEntidadPorId(id);
        return toResponse(entrenador);
    }

    @Transactional
    public EntrenadorResponse crear(EntrenadorRequest request) {
        if (entrenadorRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("DNI duplicado");
        }

        validarEspecialidad(request.getEspecialidadId());

        Entrenador entrenador = new Entrenador();
        entrenador.setNombre(request.getNombre());
        entrenador.setApellido(request.getApellido());
        entrenador.setDni(request.getDni());
        entrenador.setEspecialidadId(request.getEspecialidadId());
        entrenador.setCertificado(request.getCertificado());
        entrenador.setTelefono(request.getTelefono());
        entrenador.setEmail(request.getEmail());
        entrenador.setFotoUrl(procesarFoto(request.getFotoBase64(), "entrenador_" + request.getDni()));
        entrenador.setActivo(request.getActivo());

        Entrenador guardado = entrenadorRepository.save(entrenador);
        return toResponse(guardado);
    }

    @Transactional
    public EntrenadorResponse actualizar(Integer id, EntrenadorRequest request) {
        Entrenador entrenador = buscarEntidadPorId(id);

        entrenadorRepository.findByDni(request.getDni())
                .ifPresent(entrenadorExistente -> {
                    if (!entrenadorExistente.getId().equals(id)) {
                        throw new RuntimeException("DNI duplicado");
                    }
                });

        validarEspecialidad(request.getEspecialidadId());

        entrenador.setNombre(request.getNombre());
        entrenador.setApellido(request.getApellido());
        entrenador.setDni(request.getDni());
        entrenador.setEspecialidadId(request.getEspecialidadId());
        entrenador.setCertificado(request.getCertificado());
        entrenador.setTelefono(request.getTelefono());
        entrenador.setEmail(request.getEmail());
        entrenador.setActivo(request.getActivo());

        if (request.getFotoBase64() != null && !request.getFotoBase64().isBlank()) {
            entrenador.setFotoUrl(procesarFoto(request.getFotoBase64(), "entrenador_" + request.getDni()));
        }

        Entrenador actualizado = entrenadorRepository.save(entrenador);
        return toResponse(actualizado);
    }

    @Transactional
    public void eliminar(Integer id) {
        Entrenador entrenador = buscarEntidadPorId(id);

        // Baja lógica para no romper horarios/clases asociados.
        entrenador.setActivo(false);
        entrenadorRepository.save(entrenador);
    }

    @Transactional(readOnly = true)
    public EntrenadorCertificadoResponse obtenerCertificado(Integer id) {
        Entrenador entrenador = buscarEntidadPorId(id);

        return new EntrenadorCertificadoResponse(
                entrenador.getId(),
                entrenador.getNombre(),
                entrenador.getApellido(),
                entrenador.getCertificado()
        );
    }

    private Entrenador buscarEntidadPorId(Integer id) {
        return entrenadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el entrenador con id: " + id));
    }

    private void validarEspecialidad(Integer especialidadId) {
        Actividad actividad = actividadRepository.findById(especialidadId)
                .orElseThrow(() -> new RuntimeException("No se encontró la actividad/especialidad con id: " + especialidadId));

        if (!Boolean.TRUE.equals(actividad.getActiva())) {
            throw new RuntimeException("La actividad/especialidad seleccionada no está activa");
        }
    }

    private EntrenadorResponse toResponse(Entrenador entrenador) {
        String especialidadNombre = actividadRepository.findById(entrenador.getEspecialidadId())
                .map(Actividad::getNombre)
                .orElse(null);

        return new EntrenadorResponse(
                entrenador.getId(),
                entrenador.getNombre(),
                entrenador.getApellido(),
                entrenador.getDni(),
                entrenador.getEspecialidadId(),
                especialidadNombre,
                entrenador.getCertificado(),
                entrenador.getTelefono(),
                entrenador.getEmail(),
                fotoStorageService.normalizarFotoParaRespuesta(entrenador.getFotoUrl()),
                entrenador.getActivo()
        );
    }

    private String procesarFoto(String fotoBase64, String prefijoArchivo) {
        return fotoStorageService.guardarFoto(fotoBase64, prefijoArchivo);
    }
}
