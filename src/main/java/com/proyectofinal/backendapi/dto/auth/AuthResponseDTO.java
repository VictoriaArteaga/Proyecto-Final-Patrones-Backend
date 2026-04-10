package com.proyectofinal.backendapi.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;      // JWT
    private String username;
    private String email;
    private String role;
    private boolean requires2FA;
}