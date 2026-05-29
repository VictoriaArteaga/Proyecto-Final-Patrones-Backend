package com.proyectofinal.backendapi.dto.notification;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// Notificación tal como la consume el frontend.
@Data
@Builder
public class NotificationResponseDTO {
    private Long id;
    private String message;
    private String type;      // success | error | info
    private boolean read;
    private LocalDateTime createdAt; // se serializa como ISO; el frontend lo pasa a ms
}
