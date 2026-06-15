package com.cuerposano.backend.services;

import com.cuerposano.backend.entities.Usuario;
import com.cuerposano.backend.repositories.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.jwt.secret:cuerpo-sano-demo-secret-key-change-me-please-2026}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    public JwtService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String generarToken(Usuario usuario) {
        long now = Instant.now().getEpochSecond();
        long exp = now + Math.max(expirationMs / 1000, 300);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", usuario.getNombreUsuario());
        payload.put("usuarioId", usuario.getId());
        payload.put("rol", usuario.getRol());
        payload.put("iat", now);
        payload.put("exp", exp);

        String headerBase64 = base64Url(json(header));
        String payloadBase64 = base64Url(json(payload));
        String unsignedToken = headerBase64 + "." + payloadBase64;
        String signature = firmar(unsignedToken);

        return unsignedToken + "." + signature;
    }

    public Usuario obtenerUsuarioDesdeAuthorization(String authorizationHeader) {
        String token = extraerToken(authorizationHeader);
        return obtenerUsuarioDesdeToken(token);
    }

    public Usuario obtenerUsuarioDesdeToken(String token) {
        Map<String, Object> claims = obtenerClaims(token);
        String username = leerString(claims, "sub");

        if (username == null || username.isBlank()) {
            throw unauthorized();
        }

        return usuarioRepository.findByNombreUsuario(username)
                .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
                .orElseThrow(this::unauthorized);
    }

    public boolean esTokenValido(String token) {
        try {
            obtenerClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extraerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw unauthorized();
        }

        String value = authorizationHeader.trim();

        if (!value.toLowerCase().startsWith("bearer ")) {
            throw unauthorized();
        }

        String token = value.substring("Bearer ".length()).trim();

        if (token.isBlank()) {
            throw unauthorized();
        }

        return token;
    }

    private Map<String, Object> obtenerClaims(String token) {
        try {
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                throw new IllegalArgumentException("JWT inválido");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = firmar(unsignedToken);

            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new IllegalArgumentException("Firma inválida");
            }

            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8
            );

            Map<String, Object> claims = objectMapper.readValue(
                    payloadJson,
                    new TypeReference<Map<String, Object>>() {}
            );

            long exp = leerLong(claims, "exp");

            if (exp <= Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("JWT expirado");
            }

            return claims;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw unauthorized();
        }
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar JWT", ex);
        }
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String firmar(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo firmar JWT", ex);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;

        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        int max = Math.max(aBytes.length, bBytes.length);
        int result = aBytes.length ^ bBytes.length;

        for (int i = 0; i < max; i++) {
            byte aByte = i < aBytes.length ? aBytes[i] : 0;
            byte bByte = i < bBytes.length ? bBytes[i] : 0;
            result |= aByte ^ bByte;
        }

        return result == 0;
    }

    private String leerString(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private long leerLong(Map<String, Object> claims, String key) {
        Object value = claims.get(key);

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }

        return 0L;
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario no autenticado"
        );
    }
}
