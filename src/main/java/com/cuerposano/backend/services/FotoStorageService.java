package com.cuerposano.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FotoStorageService {

    private static final Pattern DATA_URL_PATTERN = Pattern.compile(
            "^data:(image/[a-zA-Z0-9.+-]+);base64,(.+)$",
            Pattern.DOTALL
    );

    private final Path fotosDirectory;

    public FotoStorageService(
            @Value("${app.fotos.dir:uploads/fotos}") String fotosDir
    ) {
        this.fotosDirectory = Paths.get(fotosDir)
                .toAbsolutePath()
                .normalize();
    }

    public String guardarFoto(String fotoBase64, String prefijoArchivo) {
        if (fotoBase64 == null || fotoBase64.isBlank()) {
            return null;
        }

        String valor = fotoBase64.trim();

        /*
         * Si por error llega una URL existente, la respetamos.
         * Esto evita pisar una foto al editar sin cargar una nueva.
         */
        if (valor.startsWith("/fotos/") ||
                valor.startsWith("http://") ||
                valor.startsWith("https://")) {
            return valor;
        }

        FotoDecodificada foto = decodificarFoto(valor);

        try {
            Files.createDirectories(fotosDirectory);

            String nombreSeguro = limpiarPrefijo(prefijoArchivo);
            String nombreArchivo = nombreSeguro + "_" +
                    UUID.randomUUID().toString().replace("-", "") +
                    "." + foto.extension();

            Path destino = fotosDirectory.resolve(nombreArchivo).normalize();

            Files.write(destino, foto.bytes());

            return "/fotos/" + nombreArchivo;
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la foto"
            );
        }
    }

    public String normalizarFotoParaRespuesta(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.isBlank()) {
            return null;
        }

        String valor = fotoUrl.trim();

        if (valor.startsWith("/fotos/") ||
                valor.startsWith("http://") ||
                valor.startsWith("https://") ||
                valor.startsWith("data:image/")) {
            return valor;
        }

        /*
         * Compatibilidad con registros viejos guardados como base64 crudo.
         * No devuelve base64 crudo al front para evitar errores 431.
         */
        FotoDecodificada foto = decodificarFoto(valor);

        return "data:" + foto.mimeType() + ";base64," +
                Base64.getEncoder().encodeToString(foto.bytes());
    }

    private FotoDecodificada decodificarFoto(String valorOriginal) {
        String valor = valorOriginal.trim();
        String mimeType = detectarMimeTypeDesdeContenido(valor);
        String contenidoBase64 = valor;

        Matcher matcher = DATA_URL_PATTERN.matcher(valor);

        if (matcher.matches()) {
            mimeType = matcher.group(1);
            contenidoBase64 = matcher.group(2);
        } else if (valor.contains("base64,")) {
            contenidoBase64 = valor.substring(valor.indexOf("base64,") + "base64,".length());
        }

        contenidoBase64 = contenidoBase64.replaceAll("\\s+", "");

        try {
            byte[] bytes = Base64.getDecoder().decode(contenidoBase64);

            if (bytes.length == 0) {
                throw new IllegalArgumentException("Imagen vacía");
            }

            return new FotoDecodificada(
                    bytes,
                    mimeType,
                    extensionDesdeMimeType(mimeType)
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La foto no tiene un formato válido"
            );
        }
    }

    private String detectarMimeTypeDesdeContenido(String valor) {
        String limpio = valor;

        if (limpio.contains("base64,")) {
            limpio = limpio.substring(limpio.indexOf("base64,") + "base64,".length());
        }

        if (limpio.startsWith("/9j/")) {
            return "image/jpeg";
        }

        if (limpio.startsWith("iVBOR")) {
            return "image/png";
        }

        if (limpio.startsWith("R0lGOD")) {
            return "image/gif";
        }

        if (limpio.startsWith("UklGR")) {
            return "image/webp";
        }

        if (limpio.startsWith("PHN2Zy")) {
            return "image/svg+xml";
        }

        return "image/jpeg";
    }

    private String extensionDesdeMimeType(String mimeType) {
        return switch (mimeType) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            default -> "jpg";
        };
    }

    private String limpiarPrefijo(String prefijoArchivo) {
        if (prefijoArchivo == null || prefijoArchivo.isBlank()) {
            return "foto";
        }

        return prefijoArchivo
                .toLowerCase()
                .replaceAll("[^a-z0-9_-]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private record FotoDecodificada(
            byte[] bytes,
            String mimeType,
            String extension
    ) {
    }
}
