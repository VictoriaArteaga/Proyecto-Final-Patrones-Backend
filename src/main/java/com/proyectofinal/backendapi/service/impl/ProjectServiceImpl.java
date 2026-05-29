package com.proyectofinal.backendapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.backendapi.ai.service.AiImageGenerationService;
import com.proyectofinal.backendapi.ai.service.impl.AiImageGenerationServiceImpl;
import com.proyectofinal.backendapi.ai.validation.ParametersValidatorFactory;
import com.proyectofinal.backendapi.dto.project.ParametersDTO;
import com.proyectofinal.backendapi.exception.BadRequestException;
import com.proyectofinal.backendapi.exception.InvalidStateException;
import com.proyectofinal.backendapi.exception.ProjectNotFoundException;
import com.proyectofinal.backendapi.model.*;
import com.proyectofinal.backendapi.repository.ProjectRepository;
import com.proyectofinal.backendapi.render3d.dto.Generate3DRequest;
import com.proyectofinal.backendapi.render3d.entity.GenerationTask;
import com.proyectofinal.backendapi.render3d.service.RenderService;
import com.proyectofinal.backendapi.service.ProjectService;
import com.proyectofinal.backendapi.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor // Inyecta dependencias finales (Repository, Storage, ObjectMapper).
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final StorageService storageService;
    private final ObjectMapper objectMapper; // Para procesar JSON de parámetros.
    private final AiImageGenerationService aiImageGenerationService;
    private final ParametersValidatorFactory parametersValidatorFactory;
    private final RenderService renderService; // Generación 3D real (Tripo) en segundo plano.

    // 1. CREAR PROYECTO CON IMAGEN INICIAL.
    @Override
    @Transactional
    public Project createProjectWithImage(MultipartFile file, User user, String name, DesignCategory category) {

        // Log con contexto de usuario.
        logger.info("El usuario {} está creando el proyecto: '{}' categoría={}",
                user.getId(), name, category);

        // Carpeta organizada por ID de usuario.
        String path = user.getId().toString();
        String imageUrl = storageService.uploadImage(file, path);

        Project project = Project.builder()
                .name(name)
                .user(user)
                .status(ProjectState.IMAGE_UPLOADED) // Estado inicial: Imagen cargada.
                .category(category)
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

    // 2b. LISTAR PROYECTOS DEL USUARIO (más recientes primero, sin eliminados).
    @Override
    public List<Project> getUserProjects(User user) {
        return projectRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .filter(p -> p.getStatus() != ProjectState.DELETED)
                .toList();
    }

    // 2c. ELIMINAR PROYECTO (borrado real, incluye versiones y parámetros por cascada).
    @Override
    @Transactional
    public void deleteProject(UUID projectId, User user) {
        Project project = getProjectById(projectId, user);
        logger.info("El usuario {} eliminó el proyecto {}.", user.getId(), projectId);
        projectRepository.delete(project);
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
    // 3. GENERAR RENDERIZADO 2D.
    @Override
    @Transactional
    public Project generateInitial2D(UUID projectId, User user, String description) {

        Project project = getProjectById(projectId, user);
        validateState(project, ProjectState.IMAGE_UPLOADED);

        // Usamos la descripción real del usuario; si viene vacía, caemos a un prompt genérico.
        String userDescription = (description != null && !description.isBlank())
                ? description
                : "Generar diseño 2D";

        // PASO 2: Usa la variable inyectada.
        aiImageGenerationService.generateInitialRender(
                projectId,
                user,
                userDescription
        );

        return getProjectById(projectId, user);
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

        // Validación específica por categoría.
        parametersValidatorFactory.forCategory(project.getCategory()).validate(projectParams);

        projectParams.setProject(project);
        project.setParameters(projectParams);

        // Cambiamos el estado para re-procesar
        project.setStatus(ProjectState.WAITING_2D_APPROVAL);

        return projectRepository.save(project);
    }

    // 6b. REGENERAR RENDER 2D (tras rechazo) con descripción detallada y parámetros.
    @Override
    @Transactional
    public Project regenerate2D(UUID projectId, User user, ParametersDTO parameters, String description) {
        Project project = getProjectById(projectId, user);

        // Validación de parámetros según la categoría del proyecto.
        if (parameters != null) {
            ProjectParameters pp = objectMapper.convertValue(parameters, ProjectParameters.class);
            parametersValidatorFactory.forCategory(project.getCategory()).validate(pp);
        }

        // Genera un nuevo render 2D (valida estado REJECTED_2D internamente) -> WAITING_2D_APPROVAL.
        aiImageGenerationService.generateRenderWithParameters(projectId, user, parameters, description);

        return getProjectById(projectId, user);
    }

    // 7. GENERATE 3D MODEL.
    // Nota: NO es @Transactional a propósito. La tarea de render debe quedar
    // confirmada en su propia transacción ANTES de lanzar el proceso async,
    // si no, el hilo en segundo plano no encontraría la tarea (condición de carrera).
    @Override
    public Project generate3D(UUID projectId, User user) {
        Project project = getProjectById(projectId, user);
        // Permitimos generar desde la aprobación final o reintentar tras un fallo.
        validateState(project, ProjectState.WAITING_FINAL_APPROVAL, ProjectState.FAILED);

        logger.info("Iniciando la fase final de generación 3D para el proyecto: {}", projectId);

        // 1. Marcamos el proyecto como "generando 3D" (el frontend hace polling de esto).
        project.setStatus(ProjectState.GENERATING_3D_MODEL);
        projectRepository.save(project);

        // 2. Imagen base para Tripo: el render 2D aprobado (o la original como respaldo).
        String imageUrl = (project.getImage2DUrl() != null && !project.getImage2DUrl().isBlank())
                ? project.getImage2DUrl()
                : project.getImageOriginalUrl();

        // 3. Creamos la tarea (se confirma en su propia transacción) y lanzamos el proceso async.
        //    Al terminar, RenderService escribe model3DUrl + COMPLETED en el proyecto.
        Generate3DRequest request = new Generate3DRequest();
        request.setProjectId(projectId);
        request.setImageUrl(imageUrl);

        GenerationTask task = renderService.startGeneration(request);
        renderService.processAsync(task.getId(), imageUrl);

        return getProjectById(projectId, user);
    }

    // 8. DETENER LA GENERACIÓN 3D EN CURSO.
    // Vuelve el proyecto a WAITING_FINAL_APPROVAL (estado reanudable). El proceso
    // async de Tripo no puede abortarse, pero RenderService verifica el estado
    // antes de escribir el resultado, así que un trabajo cancelado se descarta.
    @Override
    @Transactional
    public Project cancel3D(UUID projectId, User user) {
        Project project = getProjectById(projectId, user);
        validateState(project, ProjectState.GENERATING_3D_MODEL);

        logger.info("El usuario {} detuvo la generación 3D del proyecto {}.", user.getId(), projectId);
        project.setStatus(ProjectState.WAITING_FINAL_APPROVAL);

        return projectRepository.save(project);
    }

}