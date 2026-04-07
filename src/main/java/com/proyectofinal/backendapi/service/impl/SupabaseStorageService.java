package com.proyectofinal.backendapi.service.impl;

import com.proyectofinal.backendapi.config.SupabaseConfig;
import com.proyectofinal.backendapi.exception.BadRequestException;
import com.proyectofinal.backendapi.exception.InvalidStateException;
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
    private final SupabaseConfig supabaseConfig;
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

        String fileName = path + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        String uploadUrl = String.format("%s/storage/v1/object/%s/%s",
                supabaseConfig.getUrl(),
                supabaseConfig.getBucket(),
                fileName
        );

        try {
            byte[] fileBytes = file.getBytes();

            String contentType = (file.getContentType() != null)
                    ? file.getContentType()
                    : "application/octet-stream";

            webClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + supabaseConfig.getKey())
                    .header("apiKey", supabaseConfig.getKey())
                    .contentType(MediaType.parseMediaType(contentType))
                    .bodyValue(fileBytes)
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        logger.error("Error de carga de Supabase: {}", body);
                                        return Mono.error(new BadRequestException("Error al subir archivo: " + body));
                                    })
                    )
                    .bodyToMono(String.class)
                    .block();

            // URL pública
            return String.format("%s/storage/v1/object/public/%s/%s",
                    supabaseConfig.getUrl(),
                    supabaseConfig.getBucket(),
                    fileName
            );

        } catch (IOException e) {
            logger.error("Error al leer el archivo: {}", e.getMessage());
            throw new BadRequestException("No se pudo leer el archivo.");
        } catch (Exception e) {
            logger.error("Error crítico al cargar el archivo: {}", e.getMessage());
            throw new InvalidStateException("No se pudo subir el archivo al storage.");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {

        try {
            String path = fileUrl.split("/object/public/" + supabaseConfig.getBucket() + "/")[1];

            String deleteUrl = String.format("%s/storage/v1/object/%s/%s",
                    supabaseConfig.getUrl(),
                    supabaseConfig.getBucket(),
                    path
            );

            webClient.delete()
                    .uri(deleteUrl)
                    .header("Authorization", "Bearer " + supabaseConfig.getKey())
                    .header("apiKey", supabaseConfig.getKey())
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        logger.error("Error al eliminar Supabase: {}", body);
                                        return Mono.error(new BadRequestException("Error al eliminar archivo."));
                                    })
                    )
                    .toBodilessEntity()
                    .block();

        } catch (Exception e) {
            logger.error("Error al eliminar el archivo: {}", e.getMessage());
            throw new InvalidStateException("No se pudo eliminar el archivo.");
        }
    }

}
