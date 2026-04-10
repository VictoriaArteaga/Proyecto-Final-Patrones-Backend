package com.proyectofinal.backendapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Correo de bienvenida al registrarse
    public void sendWelcomeEmail(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("¡Bienvenido a la plataforma de Diseño 3D!");
        message.setText("Hola " + username + ",\n\n"
                + "Tu cuenta ha sido creada exitosamente.\n\n"
                + "¡Empieza a diseñar!\n\nEl equipo.");
        mailSender.send(message);
    }

    // Correo de recuperación de contraseña
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = "https://proyecto-final-patrones-frontend2.vercel.app/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Recuperación de contraseña");
        message.setText("Hola,\n\n"
                + "Recibimos una solicitud para restablecer tu contraseña.\n\n"
                + "Haz clic en el siguiente enlace (válido por 30 minutos):\n"
                + resetLink + "\n\n"
                + "Si no solicitaste esto, ignora este correo.\n\nEl equipo.");
        mailSender.send(message);
    }

    // Correo con código 2FA
    public void sendTwoFactorCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Tu código de verificación");
        message.setText("Tu código de verificación es: " + code + "\n\n"
                + "Este código expira en 10 minutos.\n\n"
                + "Si no iniciaste sesión, ignora este mensaje.");
        mailSender.send(message);
    }
}
