package com.proyectofinal.backendapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
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
        logger.info("User {} is creating project: '{}'", user.getId(), name);

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
            logger.error("State conflict in project {}: current {}, expected {}",
                    project.getId(), project.getStatus(), Arrays.toString(expectedStates));
            throw new IllegalStateException("Operation not permitted in the current state.");
        }
    }

    // 3. GENERAR RENDERIZADO 2D.
    @Override
    @Transactional
    public Project generateInitial2D(UUID projectId, User user) {
        Project project = getProjectById(projectId, user);
        validateState(project, ProjectState.IMAGE_UPLOADED);

        logger.info("Starting asynchronous 2D generation for project: {}", projectId);

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

        logger.info("User {} approved 2D design for project {}", user.getId(), projectId);
        project.setStatus(ProjectState.WAITING_FINAL_APPROVAL);

        return projectRepository.save(project);
    }

    // 5. RECHAZAR EL DISEÑO 2D.
    @Override
    @Transactional
    public Project rejectProject(UUID projectId, User user) {
        Project project = getProjectById(projectId, user);
        validateState(project, ProjectState.WAITING_2D_APPROVAL);

        logger.warn("Project {} rejected by user {}. Waiting for new parameters.", projectId, user.getId());
        project.setStatus(ProjectState.REJECTED_2D);

        return projectRepository.save(project);
    }

    // 6. ACTUALIZAR PARÁMETROS (Después del rechazo).
    @Override
    @Transactional
    public Project updateParameters(UUID projectId, User user, Map<String, Object> params) {
        Project project = getProjectById(projectId, user);
        validateState(project, ProjectState.REJECTED_2D);

        logger.info("User {} updating parameters for project {}", user.getId(), projectId);

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

        logger.info("Starting final 3D generation phase for project: {}", projectId);

        // Simulación de inicio de proceso 3D.
        project.setStatus(ProjectState.GENERATING_3D_MODEL);

        return projectRepository.save(project);
    }
}