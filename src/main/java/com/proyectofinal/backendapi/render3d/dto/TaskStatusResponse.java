package com.proyectofinal.backendapi.render3d.dto;


import com.proyectofinal.backendapi.render3d.entity.TaskStatus;
import com.proyectofinal.backendapi.render3d.entity.TaskType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;


// Respuesta unificada para consultar el estado de una tarea.
// Devuelta tanto al crear como al hacer polling.

@Data
@Builder
public class TaskStatusResponse {

    private UUID taskId;
    private UUID projectId;
    private TaskType type;
    private TaskStatus status;

//   URL del modelo .glb en Supabase. Solo presente cuando status = DONE.
    private String resultUrl;

//  Mensaje de error. Solo presente cuando status = ERROR.
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}