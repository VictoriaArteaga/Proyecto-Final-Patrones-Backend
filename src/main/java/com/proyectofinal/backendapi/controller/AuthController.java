package com.proyectofinal.backendapi.controller;


import com.proyectofinal.backendapi.dto.auth.*;
import com.proyectofinal.backendapi.security.AuthCookieFactory;
import com.proyectofinal.backendapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthCookieFactory authCookieFactory;

    // REGISTRO (auto-login: deja la sesión iniciada vía cookie).
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return withAuthCookie(userService.register(dto));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return withAuthCookie(userService.login(dto));
    }

    // Verificar código 2FA después del login
    @PostMapping("/verify-2fa")
    public ResponseEntity<AuthResponseDTO> verifyTwoFactor(@Valid @RequestBody TwoFactorVerifyDTO dto) {
        return withAuthCookie(userService.verifyTwoFactorCode(dto));
    }

    // LOGOUT: borra la cookie de sesión en el navegador.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookieFactory.clear().toString())
                .build();
    }

    // Activar o desactivar 2FA (ruta protegida)
    @PostMapping("/toggle-2fa")
    public ResponseEntity<String> toggleTwoFactor(@RequestBody TwoFactorToggleDTO dto) {
        return ResponseEntity.ok(userService.toggleTwoFactor(dto));
    }

    //usuario pide el link de recuperación
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody PasswordResetRequestDTO dto) {
        userService.requestPasswordReset(dto);
        return ResponseEntity.ok("Correo de recuperación enviado");
    }

    //usuario ingresa token + nueva contraseña
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetConfirmDTO dto) {
        userService.confirmPasswordReset(dto);
        return ResponseEntity.ok("Contraseña actualizada exitosamente");
    }

    // Si la respuesta trae JWT, lo movemos a una cookie HttpOnly y lo quitamos
    // del cuerpo (así nunca queda expuesto a JavaScript). Si no hay token
    // (p. ej. falta el paso 2FA), devolvemos la respuesta tal cual.
    private ResponseEntity<AuthResponseDTO> withAuthCookie(AuthResponseDTO res) {
        if (res.getToken() == null) {
            return ResponseEntity.ok(res);
        }
        String cookie = authCookieFactory.create(res.getToken()).toString();
        res.setToken(null);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie)
                .body(res);
    }
}
