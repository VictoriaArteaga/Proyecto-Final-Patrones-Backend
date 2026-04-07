package com.proyectofinal.backendapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String username;

    @Email(message = "Correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    // Opcional — para recuperación
    @Email(message = "Correo de recuperación inválido")
    private String recoveryEmail;
}