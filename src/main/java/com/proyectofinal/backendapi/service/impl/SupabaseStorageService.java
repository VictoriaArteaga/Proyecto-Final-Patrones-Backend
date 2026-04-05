package com.proyectofinal.backendapi.service.impl;

import com.proyectofinal.backendapi.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;

import java.io.IOException;


// Facade.
@RequiredArgsConstructor
@Service
public class SupabaseStorageService implements StorageService{

    private static final Logger logger = LoggerFactory.getLogger(SupabaseStorageService.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    private final WebClient webClient;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        return uploadFile(file, folder);
    }

    @Override
    public String uploadModel(MultipartFile file, String folder) {
        return uploadFile(file, folder + "/models");
    }

    // Método privado generíco.
    private String uploadFile(MultipartFile file, String path) {

        // 1. Generar el nombre único del archivo
        String fileName = path + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String url = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, fileName);

        try {
            // 2. Extraer datos del archivo de forma segura antes del WebClient.
            byte[] fileBytes = file.getBytes();
            String contentType = (file.getContentType() != null) ? file.getContentType() : "application/octet-stream";

            // 3. Ejecutar la petición a Supabase.
            webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("apiKey", supabaseKey)
                    .contentType(MediaType.parseMediaType(contentType))
                    .bodyValue(fileBytes)
                    .retrieve()
                    // Manejo de errores HTTP de Supabase (4xx, 5xx)
                    .onStatus(status -> !status.is2xxSuccessful(), response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("Supabase Error: " + body)))
                    )
                    .bodyToMono(String.class)
                    .block(); // Esperamos el resultado de forma síncrona.

            // 4. Retornar la URL pública del archivo subido.
            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, fileName);

        } catch (IOException e) {
            logger.error("Error reading file bytes: {}", e.getMessage());
            throw new RuntimeException("Could not read the uploaded file.");
        } catch (Exception e) {
            logger.error("Critical error uploading file to Supabase: {}", e.getMessage());
            throw new RuntimeException("The file could not be uploaded to the cloud.");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String path = fileUrl.split("/object/public/" + bucketName + "/")[1];
            String url = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, path);

            webClient.delete()
                    .uri(url)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("apiKey", supabaseKey)
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), response -> Mono.error(new RuntimeException("Delete failed")))
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            logger.error("The file could not be deleted from Supabase: {}", e.getMessage());
        }
    }

}
