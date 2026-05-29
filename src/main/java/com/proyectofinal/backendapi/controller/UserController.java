package com.proyectofinal.backendapi.controller;

import com.proyectofinal.backendapi.dto.user.AvatarRequestDTO;
import com.proyectofinal.backendapi.dto.user.UserProfileDTO;
import com.proyectofinal.backendapi.model.User;
import com.proyectofinal.backendapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Datos del usuario autenticado (para la vista "Mi perfil").
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(toProfileDTO(user));
    }

    // Guardar / actualizar la foto de perfil (ligada a la cuenta).
    @PutMapping("/me/avatar")
    public ResponseEntity<UserProfileDTO> updateAvatar(
            @Valid @RequestBody AvatarRequestDTO body,
            @AuthenticationPrincipal User user
    ) {
        User updated = userService.updateAvatar(user, body.getAvatar());
        return ResponseEntity.ok(toProfileDTO(updated));
    }

    // Eliminar la foto de perfil.
    @DeleteMapping("/me/avatar")
    public ResponseEntity<Void> deleteAvatar(
            @AuthenticationPrincipal User user
    ) {
        userService.deleteAvatar(user);
        return ResponseEntity.noContent().build();
    }

    // Mapeo entidad -> DTO (sin contraseña ni tokens).
    private UserProfileDTO toProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .recoveryEmail(user.getRecoveryEmail())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
