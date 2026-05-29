package com.proyectofinal.backendapi.dto.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Body para crear una notificación.
@Data
public class NotificationRequestDTO {
    @NotBlank
    private String message;

    // success | error | info. Si llega vacío, el servicio usa "info".
    private String type;
}
