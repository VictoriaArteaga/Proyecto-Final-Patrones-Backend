package com.proyectofinal.backendapi.dto.project;

import lombok.Data;

@Data
public class Generate2DRequestDTO {

    // Descripción libre del usuario sobre lo que desea generar.
    // Se inyecta en el prompt inicial (buildInitialPrompt) como requerimiento obligatorio.
    private String description;
}
