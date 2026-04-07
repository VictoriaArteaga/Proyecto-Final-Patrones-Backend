package com.proyectofinal.backendapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequestDTO {

    @Email
    @NotBlank(message = "El correo es obligatorio")
    private String email; // correo con el que se registró
}