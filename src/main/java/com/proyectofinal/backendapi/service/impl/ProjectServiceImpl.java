package com.proyectofinal.backendapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.backendapi.dto.project.ParametersDTO;
import com.proyectofinal.backendapi.exception.InvalidStateException;
import com.proyectofinal.backendapi.exception.ProjectNotFoundException;
import com.proyectofinal.backendapi.model.*;
import com.proyectofinal.backendapi.repository.ProjectRepository;
import com.proyectofinal.backendapi.service.ProjectService;
import com.proyectofinal.backendapi.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor // Inyecta dependencias finales (Repository, Storage, ObjectMapper).
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final StorageService storageService;
    private final ObjectMapper objectMapper; // Para procesar JSON de parámetros.

    // 1. CREAR PROYECTO CON IMAGEN INICIAL.
    @Override
    @Transactional
    public Project createProjectWithImage(MultipartFile file, User user, String name) {

        // Log con contexto de usuario.
        logger.info("El usuario {} está creando el proyecto: '{}'", user.getId(), name);

        // Carpeta organizada por ID de usuario.
        String path = user.getId().toString();
        String imageUrl = storageService.uploadImage(file, path);

        Project project = Project.builder()
                .name(name)
                .user(user)
                .status(ProjectState.IMAGE_UPLOADED) // Estado inicial: Imagen cargada.
                .imageOriginalUrl(imageUrl)
                .build();

        return projectRepository.save(project);
    }

    // 2. OBTENER PROYECTO POR ID.
    @Override
    public Project getProjectById(UUID id, User user) {

        // Uso de excepción personalizada.
        return projectRepository.findByIdAndUser(id, user)
                .orElseThrow(ProjectNotFoundException::new);
    }

    // VALIDACIÓN DE ESTADO.
    private void validateState(Project project, ProjectState... expectedStates) {
        boolean isValid = Arrays.asList(expectedStates).contains(project.getStatus());
        if (!isValid) {
            logger.error("Conflicto de estado en el proyecto {}: actual {}, esperado {}",
                    project.getId(), project.getStatus(), Arrays.toString(expectedStates));
            throw new InvalidStateException(
                    "Estado inválido. Estado actual: " + project.getStatus()
                            + ", estados permitted: " + Arrays.toString(expectedStates)
            );
        }
    }

    // 3. GENERAR RENDERIZADO 2D.
    @Override
    @Transactional
    public Project generateInitial2D(UUID projectId, User user) {
        Project project = getProjectById(projectId, user);
        validateState(project, ProjectState.IMAGE_UPLOADED);

        logger.info("Iniciando la generación 2D asíncrona para el proyecto: {}", projectId);

        // Aquí se llamará a la IA y el estado será GENERATING_2D.
        // Por ahora, pasamos directamente al siguiente estado lógico.
        project.setStatus(ProjectState.WAITING_2D_APPROVAL);

        return projectRepository.save(project);
    }

    // 4. APROBAR DISEÑO 2D.
    @Override
    @Transactional
    public Project approveProject(UUID projectId, User user) {
        Project project = getProjectById(projectId, user);
        validateState(project, ProjectState.WAITING_2D_APPROVAL);

        logger.info("El usuario {} aprobó el diseño 2D para el proyecto {}.", user.getId(), projectId);
        project.setStatus(ProjectState.WAITING_FINAL_APPROVAL);

        return projectRepository.save(project);
    }

    // 5. RECHAZAR EL DISEÑO 2D.
    @Override
    @Transactional
    public Project rejectProject(UUID projectId, User user) {
        Project project = getProjectById(projectId, user);
        validateState(project, ProjectState.WAITING_2D_APPROVAL);

        logger.warn("Proyecto {} rechazado por el usuario {}. Esperando nuevos parámetros.", projectId, user.getId());
        project.setStatus(ProjectState.REJECTED_2D);

        return projectRepository.save(project);
    }

    // 6. ACTUALIZAR PARÁMETROS (Después del rechazo).
    @Override
    @Transactional
    public Project updateParameters(UUID projectId, User user, ParametersDTO params) {
        Project project = getProjectById(projectId, user);
        validateState(project, ProjectState.REJECTED_2D);

        logger.info("El usuario {} está actualizando los parámetros del proyecto {}.", user.getId(), projectId);

        ProjectParameters projectParams = objectMapper.convertValue(params, ProjectParameters.class);

        projectParams.setProject(project);
        project.setParameters(projectParams);

        // Cambiamos el estado para re-procesar
        project.setStatus(ProjectState.WAITING_2D_APPROVAL);

        return projectRepository.save(project);
    }

    // 7. GENERATE 3D MODEL.
    @Override
    @Transactional
    public Project generate3D(UUID projectId, User user) {
        Project project = getProjectById(projectId, user);
        validateState(project, ProjectState.WAITING_FINAL_APPROVAL);

        logger.info("Iniciando la fase final de generación 3D para el proyecto: {}", projectId);

        // Simulación de inicio de proceso 3D.
        project.setStatus(ProjectState.GENERATING_3D_MODEL);

        return projectRepository.save(project);
    }
}