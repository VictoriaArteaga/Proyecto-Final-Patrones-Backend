package com.proyectofinal.backendapi.ai.mapper;

import com.proyectofinal.backendapi.ai.dto.GenerateRenderResponseDTO;
import com.proyectofinal.backendapi.model.Project;


public class AiMapper {

    private AiMapper() {
        // utilidad estática.
    }

    public static GenerateRenderResponseDTO toResponse(Project project, String message) {
        return GenerateRenderResponseDTO.builder()
                .projectId(project.getId())
                .render2DUrl(project.getImage2DUrl())
                .status(project.getStatus().name())
                .message(message)
                .build();
    }
}
