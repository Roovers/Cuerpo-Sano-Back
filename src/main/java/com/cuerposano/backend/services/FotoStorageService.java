package com.cuerposano.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    private final HttpClient httpClient;
    private final String supabaseUrl;
    private final String supabaseServiceRoleKey;
    private final String bucket;

    public FotoStorageService(
            @Value("${app.supabase.url:}") String supabaseUrl,
            @Value("${app.supabase.service-role-key:}") String supabaseServiceRoleKey,
            @Value("${app.supabase.bucket:fotos}") String bucket
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.supabaseUrl = limpiarBarraFinal(supabaseUrl);
        this.supabaseServiceRoleKey = supabaseServiceRoleKey;
        this.bucket = bucket;
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

        validarConfiguracionSupabase();

        FotoDecodificada foto = decodificarFoto(valor);

        String nombreSeguro = limpiarPrefijo(prefijoArchivo);
        String nombreArchivo = nombreSeguro + "_" +
                UUID.randomUUID().toString().replace("-", "") +
                "." + foto.extension();

        String carpeta = nombreSeguro.startsWith("entrenador") ? "entrenadores" : "socios";
        String rutaStorage = carpeta + "/" + nombreArchivo;

        subirASupabaseStorage(rutaStorage, foto.bytes(), foto.mimeType());

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + rutaStorage;
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

    private void subirASupabaseStorage(String rutaStorage, byte[] bytes, String mimeType) {
        String endpoint = supabaseUrl + "/storage/v1/object/" + bucket + "/" + rutaStorage;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + supabaseServiceRoleKey)
                .header("apikey", supabaseServiceRoleKey)
                .header("Content-Type", mimeType)
                .header("Cache-Control", "3600")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se pudo subir la foto a Supabase Storage: " + response.body()
                );
            }
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo conectar con Supabase Storage"
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Se interrumpió la subida de la foto a Supabase Storage"
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "La URL de Supabase Storage no es válida"
            );
        }
    }

    private void validarConfiguracionSupabase() {
        if (supabaseUrl == null || supabaseUrl.isBlank() ||
                supabaseServiceRoleKey == null || supabaseServiceRoleKey.isBlank() ||
                bucket == null || bucket.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Falta configurar Supabase Storage en el backend"
            );
        }
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

    private String limpiarBarraFinal(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim().replaceAll("/+$", "");
    }

    private record FotoDecodificada(
            byte[] bytes,
            String mimeType,
            String extension
    ) {
    }
}
