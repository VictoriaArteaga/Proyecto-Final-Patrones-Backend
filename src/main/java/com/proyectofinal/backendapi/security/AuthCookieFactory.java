package com.proyectofinal.backendapi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Construye la cookie que transporta el JWT.
 *
 * Atributos:
 *  - HttpOnly  : JavaScript NO puede leerla → mitiga robo de token por XSS.
 *  - Secure    : solo viaja por HTTPS.
 *  - SameSite=None : necesaria porque el frontend (Vercel) y el backend
 *                    (Render) están en dominios distintos (cookie "de terceros").
 *  - Path=/    : se envía a toda la API.
 */
@Component
public class AuthCookieFactory {

    // Nombre de la cookie (lo lee también el JwtAuthFilter).
    public static final String COOKIE_NAME = "token";

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // Cookie con el JWT y vencimiento igual al del token.
    public ResponseCookie create(String token) {
        return base(token)
                .maxAge(Duration.ofMillis(expirationMs))
                .build();
    }

    // Cookie vacía y expirada → borra la del navegador (logout).
    public ResponseCookie clear() {
        return base("")
                .maxAge(0)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/");
    }
}
