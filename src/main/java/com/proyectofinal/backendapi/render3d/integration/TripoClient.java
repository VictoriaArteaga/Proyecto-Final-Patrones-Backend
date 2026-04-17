package com.proyectofinal.backendapi.render3d.integration;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

// Usa WebClient (ya está en el proyecto con webflux).
// Es no-bloqueante: no congela el servidor mientras espera.

@Slf4j
@Component
@RequiredArgsConstructor
public class TripoClient {

    private static final String BASE_URL = "https://api.tripo3d.ai/v2/openapi";

    @Value("${tripo.api-key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;

//    ─── Crear tarea ────────────────────────────────────────────────────
//     Le manda la imagen a TripoAI y devuelve el ID de la tarea.
//     Ese ID lo usamos después para preguntar si ya terminó.

    public String createImageTo3DTask(String imageUrl) {
        log.info("[TripoAI] Enviando imagen: {}", imageUrl);

        Map<String, Object> body = Map.of(
                "type", "image_to_model",
                "file", Map.of(
                        "type", inferFileType(imageUrl),
                        "url",  imageUrl
                )
        );

        TripoCreateResponse response = buildClient()
                .post()
                .uri("/task")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(TripoCreateResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block(); // bloqueamos aquí porque esto es rápido (solo crea la tarea)

        if (response == null || response.getCode() != 0) {
            throw new TripoException("TripoAI no pudo crear la tarea");
        }

        String taskId = response.getData().getTaskId();
        log.info("[TripoAI] Tarea creada: {}", taskId);
        return taskId;
    }

    // ─── Consultar estado ─────────────────────────────────────────────────
//     Pregunta a TripoAI cómo va la tarea.
//     Devuelve el estado: queued, running, success, failed.

    public TripoTaskData getTaskStatus(String tripoTaskId) {
        log.debug("[TripoAI] Consultando tarea: {}", tripoTaskId);

        TripoStatusResponse response = buildClient()
                .get()
                .uri("/task/" + tripoTaskId)
                .retrieve()
                .bodyToMono(TripoStatusResponse.class)
                .timeout(Duration.ofSeconds(15))
                .block();

        if (response == null || response.getCode() != 0) {
            throw new TripoException("Error consultando tarea: " + tripoTaskId);
        }

        return response.getData();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private WebClient buildClient() {
        return webClientBuilder
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    private String inferFileType(String url) {
        String lower = url.toLowerCase();
        if (lower.contains(".png"))  return "png";
        if (lower.contains(".webp")) return "webp";
        return "jpg";
    }

    // ─── Clases internas para parsear la respuesta de TripoAI ─────────────

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TripoCreateResponse {
        private int code;
        private TaskIdData data;

        @Data @JsonIgnoreProperties(ignoreUnknown = true)
        public static class TaskIdData {
            @JsonProperty("task_id") private String taskId;
        }
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TripoStatusResponse {
        private int code;
        private TripoTaskData data;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TripoTaskData {
        @JsonProperty("task_id") private String taskId;
        private String status;   // queued | running | success | failed
        private int    progress;
        private String message;
        private Result result;

        @Data @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Result {
            private ModelFile model;

            @Data @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ModelFile {
                private String url;
            }
        }

        public String getModelUrl() {
            if (result == null || result.getModel() == null) return null;
            return result.getModel().getUrl();
        }
    }
}