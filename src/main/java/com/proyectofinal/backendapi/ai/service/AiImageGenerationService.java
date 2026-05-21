package com.proyectofinal.backendapi.ai.service;

import com.proyectofinal.backendapi.ai.dto.GenerateRenderResponseDTO;
import com.proyectofinal.backendapi.dto.project.ParametersDTO;
import com.proyectofinal.backendapi.model.User;

import java.util.UUID;


public interface AiImageGenerationService {

    // Render 2D inicial: estado IMAGE_UPLOADED -> WAITING_2D_APPROVAL.
    GenerateRenderResponseDTO generateInitialRender(UUID projectId,
                                                   User user,
                                                   String userDescription);

    // Render 2D con parámetros: estado REJECTED_2D -> WAITING_2D_APPROVAL.
    // Si "parameters" es null, se usan los del proyecto persistidos.
    GenerateRenderResponseDTO generateRenderWithParameters(UUID projectId,
                                                          User user,
                                                          ParametersDTO parameters,
                                                          String userDescription);
}
