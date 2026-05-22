package com.proyectofinal.backendapi.ai.controller;

import com.proyectofinal.backendapi.ai.dto.GenerateRenderRequestDTO;
import com.proyectofinal.backendapi.ai.dto.GenerateRenderResponseDTO;
import com.proyectofinal.backendapi.ai.service.AiImageGenerationService;
import com.proyectofinal.backendapi.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoint REST para la generación de render 2D vía IA.

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiImageGenerationController {

    private final AiImageGenerationService aiImageGenerationService;

    @PostMapping("/render-2d")
    public ResponseEntity<GenerateRenderResponseDTO> generateRender(
            @Valid @RequestBody GenerateRenderRequestDTO request,
            @AuthenticationPrincipal User user
    ) {

        log.info("[AiController] POST /api/ai/render-2d projectId={} mode={}",
                request.getProjectId(), request.getMode());

        GenerateRenderResponseDTO response = switch (request.getMode()) {
            case INITIAL -> aiImageGenerationService.generateInitialRender(
                    request.getProjectId(),
                    user,
                    request.getDescription()
            );
            case WITH_PARAMETERS -> aiImageGenerationService.generateRenderWithParameters(
                    request.getProjectId(),
                    user,
                    request.getParameters(),
                    request.getDescription()
            );
        };

        return ResponseEntity.ok(response);
    }
}
