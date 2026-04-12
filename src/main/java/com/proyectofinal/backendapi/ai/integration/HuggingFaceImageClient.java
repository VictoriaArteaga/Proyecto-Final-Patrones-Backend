package com.proyectofinal.backendapi.ai.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class HuggingFaceImageClient {

    private final WebClient webClient = WebClient.builder().build();

    // INYECCIÓN DE PROPIEDADES.

    // 1. Acceso a la Api key.
    @Value("${huggingface.api.key}")
    private String apiKey;

    // 2. Acceso a la url de la AI.
    @Value("${huggingface.api.url}")
    private String baseUrl;

    // 2. Acceso al modelo de la AI.
    @Value("${huggingface.api.model}")
    private String model;

    // byte[]: Devuelve un arreglo de bytes. La AI devuelve un archivo binario de la imagen.
    public byte[] generateImageFromImage(String prompt, byte[] terrainImage) {

        // Validación temprana de la imagen del terreno.
        if (terrainImage == null || terrainImage.length == 0) {
            throw new IllegalArgumentException("La imagen del terreno es obligatoria para este método.");
        }

        // Convertir los bytes de la imagen del terreno a una cadena Base64.
        String base64Image = java.util.Base64.getEncoder().encodeToString(terrainImage);

        // Cuerpo con la estructura que espera la API.
        RequestBody body = new RequestBody(
                new Inputs(prompt, base64Image)
        );

        try {
            // Envío de datos al servidor de Hugging face.
            return webClient.post()

                    // uri: Construye la dirección final.
                    .uri(baseUrl + model)

                    // AUTHORIZATION: envía el token al servidor de hugging face.
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)

                    //CONTENT_TYPE: Le avisa a la AI que el cuerpo de la petición es formato JSON.
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

                    // Instancia de record RequestBody con el prompt de PromptBuilder.
                    .bodyValue(body)

                    // Envía la petición y espera la respuesta.
                    .retrieve()
                    .bodyToMono(byte[].class) // Convierte el cuerpo de la respuesta en bytes.
                    .timeout(Duration.ofSeconds(60))

                    // Transforma la petición de asíncrona a síncrona.
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar la imagen con HuggingFace", e);
        }
    }

    // Creación del JSON.
    private record RequestBody(Inputs inputs) {}

    private record Inputs(String prompt, String image) {}
}

