package com.proyectofinal.backendapi.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

// Respuesta del endpoint POST /api/ai/render-2d
@Data
@Builder
public class GenerateRenderResponseDTO {

    private UUID projectId;
    private String render2DUrl;
    private String status;   // Estado del proyecto tras la generación.
    private String message;  // Mensaje informativo para el cliente.
}
