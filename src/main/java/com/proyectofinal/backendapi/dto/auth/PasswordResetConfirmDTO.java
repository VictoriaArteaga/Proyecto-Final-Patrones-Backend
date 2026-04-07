package com.proyectofinal.backendapi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetConfirmDTO {

    @NotBlank
    private String token; // el token que llegó por correo

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @NotBlank
    private String newPassword;
}