package com.proyectofinal.backendapi.render3d.integration;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

//Respuesta de la API de TripoAI al consultar el estado de una tarea.

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripoTaskStatusResponse {

    private int code;
    private TaskData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskData {

        @JsonProperty("task_id")
        private String taskId;

//        queued | running | success | failed | cancelled
        private String status;

//        Porcentaje de progreso 0-100
        private int progress;

        private String message;
        private Result result;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private ModelFile model;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelFile {
        private String url;
    }
}