package com.proyectofinal.backendapi.ai.dto;

import com.proyectofinal.backendapi.dto.project.ParametersDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

// Cuerpo del request para POST /api/ai/render-2d
@Data
public class GenerateRenderRequestDTO {

    @NotNull(message = "El projectId es obligatorio")
    private UUID projectId;

    @NotNull(message = "El modo (INITIAL o WITH_PARAMETERS) es obligatorio")
    private RenderMode mode;

    private String description;
    private ParametersDTO parameters;
}
