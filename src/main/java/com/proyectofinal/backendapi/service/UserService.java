package com.proyectofinal.backendapi.service;

import com.proyectofinal.backendapi.dto.auth.*;
import com.proyectofinal.backendapi.model.User;
import com.proyectofinal.backendapi.repository.UserRepository;
import com.proyectofinal.backendapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;


    // REGISTRO
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) //  encriptada
                .recoveryEmail(dto.getRecoveryEmail())
                .role(User.Role.USER)
                .build();

        userRepository.save(user);

        // Enviar correo de bienvenida
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponseDTO(token, user.getUsername(), user.getEmail(), user.getRole().name());
    }

    // LOGIN
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponseDTO(token, user.getUsername(), user.getEmail(), user.getRole().name());
    }

    // Solicitar recuperación de contraseña
    public void requestPasswordReset(PasswordResetRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("No existe cuenta con ese correo"));

        // Generar token aleatorio seguro
        String resetToken = UUID.randomUUID().toString();

        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        // Determinar a dónde enviar el correo
        String targetEmail = (user.getRecoveryEmail() != null && !user.getRecoveryEmail().isBlank())
                ? user.getRecoveryEmail()
                : user.getEmail();

        emailService.sendPasswordResetEmail(targetEmail, resetToken);
    }

    //  Confirmar nueva contraseña con el token
    public void confirmPasswordReset(PasswordResetConfirmDTO dto) {
        User user = userRepository.findByPasswordResetToken(dto.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        // Verificar que el token no haya vencido
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordResetToken(null);      // invalidar token
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }
}

