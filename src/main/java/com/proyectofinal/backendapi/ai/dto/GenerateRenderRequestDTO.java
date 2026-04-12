package com.proyectofinal.backendapi.ai.dto;

import java.util.Map;

public class GenerateRenderRequestDTO {

    private Long projectId;
    private RenderMode mode;
    private String description;
    private Map<String, Object> parameters; // Dinámico para estilos, luces, etc.

}
