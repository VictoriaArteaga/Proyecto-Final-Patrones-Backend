package com.proyectofinal.backendapi.controller;

import com.proyectofinal.backendapi.dto.user.UserProfileDTO;
import com.proyectofinal.backendapi.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    // Datos del usuario autenticado (para la vista "Mi perfil").
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(
            @AuthenticationPrincipal User user
    ) {
        UserProfileDTO dto = UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .recoveryEmail(user.getRecoveryEmail())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .build();

        return ResponseEntity.ok(dto);
    }
}
