package com.proyectofinal.backendapi.controller;

import com.proyectofinal.backendapi.dto.notification.NotificationRequestDTO;
import com.proyectofinal.backendapi.dto.notification.NotificationResponseDTO;
import com.proyectofinal.backendapi.model.Notification;
import com.proyectofinal.backendapi.model.User;
import com.proyectofinal.backendapi.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Listar las notificaciones del usuario autenticado.
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> list(
            @AuthenticationPrincipal User user
    ) {
        List<NotificationResponseDTO> notifications =
                notificationService.getUserNotifications(user)
                        .stream()
                        .map(this::toDTO)
                        .toList();
        return ResponseEntity.ok(notifications);
    }

    // Crear una notificación.
    @PostMapping
    public ResponseEntity<NotificationResponseDTO> create(
            @Valid @RequestBody NotificationRequestDTO body,
            @AuthenticationPrincipal User user
    ) {
        Notification created =
                notificationService.create(user, body.getMessage(), body.getType());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(created));
    }

    // Marcar todas como leídas.
    @PatchMapping("/read")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal User user
    ) {
        notificationService.markAllRead(user);
        return ResponseEntity.noContent().build();
    }

    // Borrar todas las del usuario.
    @DeleteMapping
    public ResponseEntity<Void> clearAll(
            @AuthenticationPrincipal User user
    ) {
        notificationService.clearAll(user);
        return ResponseEntity.noContent().build();
    }

    // Mapeo entidad -> DTO.
    private NotificationResponseDTO toDTO(Notification n) {
        return NotificationResponseDTO.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
