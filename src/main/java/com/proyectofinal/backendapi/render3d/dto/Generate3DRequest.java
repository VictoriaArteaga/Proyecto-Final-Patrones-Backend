package com.proyectofinal.backendapi.render3d.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;


//  Cuerpo del request POST /api/render/generate-3d

@Data
public class Generate3DRequest {

    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String imageUrl;

    @NotNull(message = "El projectId es obligatorio")
    private UUID projectId;
}