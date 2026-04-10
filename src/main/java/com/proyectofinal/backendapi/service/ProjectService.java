package com.proyectofinal.backendapi.service;

import com.proyectofinal.backendapi.dto.project.ParametersDTO;
import com.proyectofinal.backendapi.model.Project;
import com.proyectofinal.backendapi.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProjectService {

    // Crear el proyecto con la imagen inicial del terreno.
    Project createProjectWithImage(MultipartFile file, User user, String name);

    // Obtener un proyecto validando que el usuario sea el dueño.
    Project getProjectById(UUID id, User user);

    // Iniciar el procesamiento de la IA para el render 2D.
    Project generateInitial2D(UUID projectId, User user);

    // Aprobar el diseño 2D para el modelo a 3D.
    Project approveProject(UUID projectId, User user);

    // Rechazar el diseño 2D para pedir cambios.
    Project rejectProject(UUID projectId, User user);

    // Actualizar parámetros después de un rechazo.
    Project updateParameters(UUID id, User user, ParametersDTO params);

    // Iniciar la generación del modelo 3D final.
    Project generate3D(UUID projectId, User user);
}
