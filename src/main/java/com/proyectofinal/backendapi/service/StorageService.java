package com.proyectofinal.backendapi.service;
import org.springframework.web.multipart.MultipartFile;

// Patrón Strategy.
public interface StorageService {

    // Sube una imagen 2D y retorna la URl pública.
    String uploadImage(MultipartFile file, String path);

    // Sube el modelo 3D OBJ y retorna la URl.
    String uploadModel(MultipartFile file, String path);

    // Elimina el archivo de la DB si el proyecto se borra.
    void deleteFile(String fileUrl);
}
