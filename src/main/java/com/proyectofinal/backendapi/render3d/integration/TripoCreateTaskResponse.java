package com.proyectofinal.backendapi.render3d.integration;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


// Respuesta de la API de TripoAI al crear una tarea.

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripoCreateTaskResponse {

    private int code;
    private TaskData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskData {
        @JsonProperty("task_id")
        private String taskId;
    }
}