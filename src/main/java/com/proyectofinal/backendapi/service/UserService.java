package com.proyectofinal.backendapi.service;

import com.proyectofinal.backendapi.dto.auth.*;
import com.proyectofinal.backendapi.model.Role;
import com.proyectofinal.backendapi.model.User;
import com.proyectofinal.backendapi.repository.UserRepository;
import com.proyectofinal.backendapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import com.proyectofinal.backendapi.exception.*;

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
            throw new BadRequestException("El correo ya está registrado");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) //  encriptada
                .recoveryEmail(dto.getRecoveryEmail())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        // Enviar correo de bienvenida
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponseDTO(token, user.getUsername(), user.getEmail(), user.getRole().name(), false);
    }

    // LOGIN
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Contraseña incorrecta");
        }

        if (user.isTwoFactorEnabled()) {
            String code = generateSixDigitCode();
            user.setTwoFactorCode(code);
            user.setTwoFactorCodeExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);

            emailService.sendTwoFactorCode(user.getEmail(), code);

            // Avisa al frontend que necesita el código
            return new AuthResponseDTO(null, user.getUsername(), user.getEmail(), user.getRole().name(), true);
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponseDTO(token, user.getUsername(), user.getEmail(), user.getRole().name(), false);
    }

    // Verifica el código 2FA-
    public AuthResponseDTO verifyTwoFactorCode(TwoFactorVerifyDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        if (user.getTwoFactorCode() == null || !user.getTwoFactorCode().equals(dto.getCode())) {
            throw new BadRequestException("Código inválido");
        }

        if (user.getTwoFactorCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El código ha expirado");
        }

        // Limpiar el código usado
        user.setTwoFactorCode(null);
        user.setTwoFactorCodeExpiry(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponseDTO(token, user.getUsername(), user.getEmail(), user.getRole().name(), false);
    }
    // Activar o desactivar 2FA
    public String toggleTwoFactor(TwoFactorToggleDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        user.setTwoFactorEnabled(dto.isEnable());
        userRepository.save(user);

        return dto.isEnable() ? "2FA activado correctamente" : "2FA desactivado correctamente";
    }

    // Genera código de 6 dígitos seguro
    private String generateSixDigitCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // siempre 6 dígitos
        return String.valueOf(code);
    }

    // Solicitar recuperación de contraseña
    public void requestPasswordReset(PasswordResetRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadRequestException("No existe cuenta con ese correo"));

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
                .orElseThrow(() -> new BadRequestException("Token inválido"));

        // Verificar que el token no haya vencido
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token ha expirado");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordResetToken(null);      // invalidar token
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    // ENCONTRAR USUARIO POR EMAIL.
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
    }

}

