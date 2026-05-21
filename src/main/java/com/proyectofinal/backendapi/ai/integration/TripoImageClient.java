package com.proyectofinal.backendapi.ai.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TripoImageClient {

    @Value("${tripo.api-key}")
    private String apiKey;

    @Value("${tripo.base-url:https://api.tripo3d.ai/v2/openapi}")
    private String baseUrl;

    private final WebClient.Builder webClientBuilder;

    public TripoImageClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    // FASE 1: Generación de la imagen apartir de la imagen inicial y texto.
    public byte[] generateInitialImage(String prompt) {

        validateConfig();

        log.info("[Tripo] Iniciando generación inicial (text_to_image)");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("type", "text_to_image");
        requestBody.put("prompt", prompt);

        String taskId = submitTask(requestBody);

        String resultImageUrl = pollForTaskResult(taskId);

        return downloadImageBytes(resultImageUrl);
    }

    // FASE 2: Generación de la imagen despues de ser rechazada por el usuario.
    public byte[] generateParameterizedImage(
            String prompt,
            Map<String, Object> advancedParams
    ) {

        validateConfig();

        log.info("[Tripo] Iniciando generación avanzada (generate_image)");

        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("type", "generate_image");

        requestBody.put(
                "model_version",
                advancedParams.getOrDefault(
                        "model_version",
                        "flux.1_kontext_pro"
                )
        );

        requestBody.put("prompt", prompt);

        // Template opcional
        if (advancedParams.containsKey("template")) {
            requestBody.put("template", advancedParams.get("template"));
        }


        if (advancedParams.containsKey("url")) {

            String imageUrl = advancedParams.get("url").toString();

            String fileType;

            if (imageUrl.toLowerCase().endsWith(".png")) {
                fileType = "image/png";
            } else {
                fileType = "image/jpeg";
            }

            Map<String, Object> fileParam = new HashMap<>();
            fileParam.put("type", fileType);
            fileParam.put("url", imageUrl);


            requestBody.put(
                    "files",
                    List.of(fileParam)
            );

            log.info(
                    "[Tripo] Imagen de referencia enviada correctamente dentro de 'files': {}",
                    imageUrl
            );
        }

        // Opcionales
        if (advancedParams.containsKey("sketch_to_render")) {
            requestBody.put(
                    "sketch_to_render",
                    advancedParams.get("sketch_to_render")
            );
        }

        if (advancedParams.containsKey("t_pose")) {
            requestBody.put(
                    "t_pose",
                    advancedParams.get("t_pose")
            );
        }

        log.info("[Tripo JSON FINAL] {}", requestBody);

        String taskId = submitTask(requestBody);
        String resultImageUrl = pollForTaskResult(taskId);
        return downloadImageBytes(resultImageUrl);

    }

    // Envío de tareas a tripo y obtiene task_id
    private String submitTask(Map<String, Object> body) {

        log.info("[Tripo] Enviando tarea con body: {}", body);

        try {

            Map<?, ?> response = buildClient()
                    .post()
                    .uri("/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(
                            status -> !status.is2xxSuccessful(),
                            clientResponse ->
                                    clientResponse.bodyToMono(String.class)
                                            .flatMap(errorBody ->
                                                    Mono.error(
                                                            new AiGenerationException(
                                                                    "Tripo API devolvió error "
                                                                            + clientResponse.statusCode()
                                                                            + ": "
                                                                            + errorBody
                                                            )
                                                    )
                                            )
                    )
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(30));

            if (response == null) {
                throw new AiGenerationException(
                        "Respuesta nula de Tripo."
                );
            }

            if (!Integer.valueOf(0).equals(response.get("code"))) {
                throw new AiGenerationException(
                        "Error en respuesta de Tripo: " + response
                );
            }

            Map<?, ?> data = (Map<?, ?>) response.get("data");

            if (data == null || data.get("task_id") == null) {
                throw new AiGenerationException(
                        "No se recibió task_id válido."
                );
            }

            String taskId = data.get("task_id").toString();

            log.info("[Tripo] Task creada correctamente: {}", taskId);

            return taskId;

        } catch (AiGenerationException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error(
                    "[Tripo] Error enviando tarea: {}",
                    ex.getMessage()
            );

            throw new AiGenerationException(
                    "Error comunicándose con Tripo: "
                            + ex.getMessage(),
                    ex
            );
        }
    }

    // Pooling activo hasta obtener resultados.
    private String pollForTaskResult(String taskId) {

        log.info("[Tripo] Iniciando polling para tarea: {}", taskId);

        int maxAttempts = 36;

        for (int i = 0; i < maxAttempts; i++) {

            try {

                Thread.sleep(5000);

                log.info(
                        "[Tripo] Polling intento {}/{} para tarea {}",
                        i + 1,
                        maxAttempts,
                        taskId
                );

                Map<?, ?> response = buildClient()
                        .get()
                        .uri("/task/" + taskId)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block(Duration.ofSeconds(20));

                if (response == null) {
                    continue;
                }

                if (!Integer.valueOf(0).equals(response.get("code"))) {
                    continue;
                }

                Map<?, ?> data = (Map<?, ?>) response.get("data");

                if (data == null) {
                    continue;
                }

                String status = data.get("status") != null
                        ? data.get("status").toString()
                        : "unknown";

                log.info(
                        "[Tripo] Estado actual de tarea {}: {}",
                        taskId,
                        status
                );

                // COMPLETADO
                if ("success".equalsIgnoreCase(status)
                        || "completed".equalsIgnoreCase(status)) {

                    log.info(
                            "[Tripo DEBUG] Data completa: {}",
                            data
                    );

                    Map<?, ?> output = (Map<?, ?>) data.get("output");

                    if (output != null) {

                        log.info(
                                "[Tripo DEBUG] Output: {}",
                                output
                        );

                        for (String key : new String[]{
                                "image",
                                "image_url",
                                "generated_image",
                                "url",
                                "result_url"
                        }) {

                            if (output.containsKey(key)
                                    && output.get(key) != null) {

                                String resultUrl =
                                        output.get(key).toString();

                                log.info(
                                        "[Tripo] URL encontrada en output.{}: {}",
                                        key,
                                        resultUrl
                                );

                                return resultUrl;
                            }
                        }
                    }

                    // fallback
                    if (data.containsKey("url")
                            && data.get("url") != null) {

                        return data.get("url").toString();
                    }

                    throw new AiGenerationException(
                            "Tripo respondió success pero sin URL válida."
                    );
                }

                // FALLÓ
                else if ("failed".equalsIgnoreCase(status)
                        || "error".equalsIgnoreCase(status)) {

                    throw new AiGenerationException(
                            "La tarea falló en Tripo. Data: " + data
                    );
                }

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                throw new AiGenerationException(
                        "Polling interrumpido.",
                        e
                );

            } catch (AiGenerationException ex) {

                throw ex;

            } catch (Exception e) {

                log.warn(
                        "[Tripo] Error durante polling: {}",
                        e.getMessage()
                );
            }
        }

        throw new AiGenerationException(
                "Timeout esperando resultado de Tripo."
        );
    }


    // Descarga del render 2d generado.
    private byte[] downloadImageBytes(String imageUrl) {
        try {
            log.info("[Tripo] Descargando imagen final desde: {}", imageUrl);

            WebClient downloadClient = WebClient.builder()
                    .codecs(configurer ->
                            configurer.defaultCodecs()
                                    .maxInMemorySize(10 * 1024 * 1024) // 10 MB
                    )
                    .build();

            byte[] bytes = downloadClient
                    .get()
                    .uri(imageUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block(Duration.ofSeconds(60));

            if (bytes == null || bytes.length == 0) {
                throw new AiGenerationException("La imagen descargada está vacía.");
            }

            log.info("[Tripo] Imagen descargada correctamente. Tamaño: {} bytes", bytes.length);

            return bytes;

        } catch (Exception ex) {
            throw new AiGenerationException(
                    "Error descargando imagen final: " + ex.getMessage(),
                    ex
            );
        }
    }

    private WebClient buildClient() {

        return webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(
                        "Authorization",
                        "Bearer " + apiKey
                )
                .build();
    }

    private void validateConfig() {

        if (apiKey == null || apiKey.isBlank()) {

            throw new AiGenerationException(
                    "Falta configurar tripo.api-key"
            );
        }
    }
}