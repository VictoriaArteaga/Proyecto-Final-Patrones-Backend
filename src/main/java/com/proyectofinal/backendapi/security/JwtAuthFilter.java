package com.proyectofinal.backendapi.security;

import com.proyectofinal.backendapi.model.User;
import com.proyectofinal.backendapi.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private UserService userService;

    // Constructor para JwtService (que no es circular)
    // Usamos @Autowired y @Lazy en el setter o campo para UserService
    public JwtAuthFilter(JwtService jwtService, @org.springframework.context.annotation.Lazy UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // El token viaja en una cookie HttpOnly; como respaldo aceptamos también
        // la cabecera Authorization: Bearer (útil para herramientas/Postman).
        String token = resolveToken(request);

        // Si no hay token, continuar sin autenticar
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtService.isTokenValid(token)) {
            String email = jwtService.extractEmail(token);

            // Traer el usuario real.
            User user = userService.findByEmail(email);

            // Ahora el principal es el user real.
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, List.of());

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    // Obtiene el JWT: primero de la cookie HttpOnly, luego de la cabecera Bearer.
    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AuthCookieFactory.COOKIE_NAME.equals(cookie.getName())
                        && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}