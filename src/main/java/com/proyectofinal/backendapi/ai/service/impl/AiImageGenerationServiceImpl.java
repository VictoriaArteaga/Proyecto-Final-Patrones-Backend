package com.proyectofinal.backendapi.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.backendapi.ai.dto.GenerateRenderResponseDTO;
import com.proyectofinal.backendapi.ai.integration.AiGenerationException;
import com.proyectofinal.backendapi.ai.integration.TripoImageClient;
import com.proyectofinal.backendapi.ai.mapper.AiMapper;
import com.proyectofinal.backendapi.ai.prompt.PromptBuilder;
import com.proyectofinal.backendapi.ai.service.AiImageGenerationService;
import com.proyectofinal.backendapi.dto.project.ParametersDTO;
import com.proyectofinal.backendapi.exception.InvalidStateException;
import com.proyectofinal.backendapi.exception.ProjectNotFoundException;
import com.proyectofinal.backendapi.model.*;
import com.proyectofinal.backendapi.repository.ProjectRepository;
import com.proyectofinal.backendapi.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiImageGenerationServiceImpl implements AiImageGenerationService {

    private final ProjectRepository projectRepository;
    private final PromptBuilder promptBuilder;
    private final TripoImageClient tripoImageClient;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public GenerateRenderResponseDTO generateInitialRender(UUID projectId,
                                                           User user,
                                                           String userDescription) {

        Project project = findProjectOrThrow(projectId, user);
        validateState(project, ProjectState.IMAGE_UPLOADED);

        log.info("[Tripo Service] Fase Inicial: Fusionando foto base + descripción para proyecto {}", projectId);
        project.setStatus(ProjectState.GENERATING_2D);
        projectRepository.save(project);

        try {
            String prompt = promptBuilder.buildInitialPrompt(project, userDescription);
            log.debug("[Tripo Service] Prompt inicial: {}", prompt);

            Map<String, Object> initialParams = new HashMap<>();
            //  flux.1_kontext_pro soporta imagen de referencia (flux.1_dev NO la soporta).
            initialParams.put("model_version", "flux.1_kontext_pro");

            if (project.getImageOriginalUrl() != null && !project.getImageOriginalUrl().isBlank()) {

                // Pasamos la URL bajo la key "url", TripoImageClient la envuelve correctamente en { "file": { "type": "jpeg/png", "url": "..." } }.
                initialParams.put("url", project.getImageOriginalUrl());
                log.info("[Tripo Service] Imagen base del proyecto será enviada como referencia: {}",
                        project.getImageOriginalUrl());
            }

            byte[] generated = tripoImageClient.generateParameterizedImage(prompt, initialParams);

            String renderUrl = uploadRender(generated, projectId, "render2d-initial.png");
            project.setImage2DUrl(renderUrl);
            project.setStatus(ProjectState.WAITING_2D_APPROVAL);
            projectRepository.save(project);

            log.info("[Tripo Service] Render inicial generado exitosamente: {}", renderUrl);
            return AiMapper.toResponse(project, "Render inicial (Foto + Texto) generado correctamente con Tripo3D.");

        } catch (Exception ex) {
            log.error("[Tripo Service] Falló la generación inicial para proyecto {}: {}",
                    projectId, ex.getMessage(), ex);
            project.setStatus(ProjectState.FAILED);
            projectRepository.save(project);
            throw new AiGenerationException("No se pudo generar el render inicial con Tripo: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    public GenerateRenderResponseDTO generateRenderWithParameters(UUID projectId,
                                                                  User user,
                                                                  ParametersDTO incomingParameters,
                                                                  String userDescription) {

        Project project = findProjectOrThrow(projectId, user);
        validateState(project, ProjectState.REJECTED_2D);

        log.info("[Tripo Service] Fase Avanzada: Refinando render con parámetros detallados para proyecto {}",
                projectId);
        project.setStatus(ProjectState.GENERATING_2D_WITH_PARAMS);
        projectRepository.save(project);

        try {
            ProjectParameters paramsToUse = resolveParameters(project, incomingParameters);
            String prompt = promptBuilder.buildParameterizedPrompt(project, paramsToUse, userDescription);
            log.debug("[Tripo Service] Prompt parametrizado: {}", prompt);

            Map<String, Object> advancedParams = new HashMap<>();
            // flux.1_kontext_pro soporta imagen de referencia.
            advancedParams.put("model_version", "flux.1_kontext_pro");

            if (project.getImageOriginalUrl() != null && !project.getImageOriginalUrl().isBlank()) {
                advancedParams.put("url", project.getImageOriginalUrl());
            }

            // Mapeo de parámetros del proyecto hacia templates de Tripo.
            if (paramsToUse.getConstructionType() != null) {
                String type = paramsToUse.getConstructionType().toLowerCase();
                if (type.contains("boceto") || type.contains("sketch")) {
                    advancedParams.put("sketch_to_render", true);
                } else if (type.contains("mueble") || type.contains("estanteria")
                        || type.contains("objeto")) {
                    advancedParams.put("template", "asset_extraction");
                } else {
                    advancedParams.put("template", "3d_enhance");
                }
            }

            byte[] generated = tripoImageClient.generateParameterizedImage(prompt, advancedParams);

            String renderUrl = uploadRender(generated, projectId, "render2d-params.png");

            addProjectVersion(project, renderUrl, prompt);

            project.setImage2DUrl(renderUrl);
            project.setStatus(ProjectState.WAITING_2D_APPROVAL);
            projectRepository.save(project);

            log.info("[Tripo Service] Render avanzado generado exitosamente: {}", renderUrl);
            return AiMapper.toResponse(project,
                    "Render refinado con parámetros generado correctamente con Tripo3D.");

        } catch (Exception ex) {
            log.error("[Tripo Service] Falló la generación parametrizada para proyecto {}: {}",
                    projectId, ex.getMessage(), ex);
            project.setStatus(ProjectState.FAILED);
            projectRepository.save(project);
            throw new AiGenerationException(
                    "No se pudo refinar el render con parámetros: " + ex.getMessage(), ex);
        }
    }



    private Project findProjectOrThrow(UUID projectId, User user) {
        return projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(ProjectNotFoundException::new);
    }

    private void validateState(Project project, ProjectState... expected) {
        boolean valid = Arrays.asList(expected).contains(project.getStatus());
        if (!valid) {
            throw new InvalidStateException("Estado inválido para la generación IA. Actual: "
                    + project.getStatus() + ", permitidos: " + Arrays.toString(expected));
        }
    }

    private ProjectParameters resolveParameters(Project project, ParametersDTO incoming) {
        if (incoming == null) {
            return project.getParameters();
        }
        ProjectParameters mapped = objectMapper.convertValue(incoming, ProjectParameters.class);
        mapped.setProject(project);
        project.setParameters(mapped);
        return mapped;
    }

    private String uploadRender(byte[] imageBytes, UUID projectId, String fileName) {
        String path = "projects/" + projectId + "/renders";
        return storageService.uploadGeneratedImage(imageBytes, path, fileName);
    }

    private void addProjectVersion(Project project, String renderUrl, String prompt) {
        List<ProjectVersion> versions = project.getVersions();
        if (versions == null) {
            versions = new ArrayList<>();
            project.setVersions(versions);
        }

        ProjectVersion version = ProjectVersion.builder()
                .versionNumber(versions.size() + 1)
                .image2DUrl(renderUrl)
                .aiDescription(prompt)
                .project(project)
                .build();

        versions.add(version);
    }
}