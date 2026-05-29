package com.proyectofinal.backendapi.service;

import com.proyectofinal.backendapi.dto.project.ParametersDTO;
import com.proyectofinal.backendapi.model.DesignCategory;
import com.proyectofinal.backendapi.model.Project;
import com.proyectofinal.backendapi.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    // Crear el proyecto con la imagen inicial y la categoría de diseño.
    Project createProjectWithImage(MultipartFile file, User user, String name, DesignCategory category);

    // Listar todos los proyectos del usuario (más recientes primero).
    List<Project> getUserProjects(User user);

    // Eliminar (borrado lógico) un proyecto del usuario.
    void deleteProject(UUID id, User user);

    // Obtener un proyecto validando que el usuario sea el dueño.
    Project getProjectById(UUID id, User user);

    // Iniciar el procesamiento de la IA para el render 2D.
    // La descripción libre del usuario se inyecta en el prompt inicial.
    Project generateInitial2D(UUID projectId, User user, String description);

    // Aprobar el diseño 2D para el modelo a 3D.
    Project approveProject(UUID projectId, User user);

    // Rechazar el diseño 2D para pedir cambios.
    Project rejectProject(UUID projectId, User user);

    // Actualizar parámetros después de un rechazo.
    Project updateParameters(UUID id, User user, ParametersDTO params);

    // Regenerar el render 2D tras un rechazo, con descripción detallada y parámetros.
    Project regenerate2D(UUID id, User user, ParametersDTO parameters, String description);

    // Iniciar la generación del modelo 3D final.
    Project generate3D(UUID projectId, User user);
}
