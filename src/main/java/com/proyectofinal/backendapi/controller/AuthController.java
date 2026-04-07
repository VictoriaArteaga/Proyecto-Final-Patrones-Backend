package com.proyectofinal.backendapi.controller;


import com.proyectofinal.backendapi.dto.auth.*;
import com.proyectofinal.backendapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // REGISTRO
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.ok(userService.register(dto));
    }
    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(userService.login(dto));
    }

    // Paso 1: usuario pide el link de recuperación
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody PasswordResetRequestDTO dto) {
        userService.requestPasswordReset(dto);
        return ResponseEntity.ok("Correo de recuperación enviado");
    }

    // Paso 2: usuario ingresa token + nueva contraseña
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetConfirmDTO dto) {
        userService.confirmPasswordReset(dto);
        return ResponseEntity.ok("Contraseña actualizada exitosamente");
    }
}
