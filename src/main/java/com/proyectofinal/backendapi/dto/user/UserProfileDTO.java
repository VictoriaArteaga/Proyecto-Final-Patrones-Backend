package com.proyectofinal.backendapi.dto.user;

import com.proyectofinal.backendapi.model.Role;
import lombok.Builder;
import lombok.Data;

// Datos públicos del usuario para mostrar en "Mi perfil" (sin contraseña ni tokens).
@Data
@Builder
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String recoveryEmail;
    private boolean twoFactorEnabled;
    private String avatarUrl;
}
