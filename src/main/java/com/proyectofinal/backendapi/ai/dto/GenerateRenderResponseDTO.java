package com.proyectofinal.backendapi.ai.dto;

import lombok.Builder;

@Builder
public class GenerateRenderResponseDTO {

    private Long projectId;
    private String render2DUrl;
    private GenerateStatus status;
    private String message;

}
