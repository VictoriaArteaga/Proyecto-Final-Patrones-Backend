package com.proyectofinal.backendapi.dto.error;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponseDTO {

    private String message;
    private int status;
    private LocalDateTime timestamp;
}
