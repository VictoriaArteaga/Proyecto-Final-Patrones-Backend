package com.proyectofinal.backendapi.dto.project;

import lombok.Data;

@Data
public class Regenerate2DRequestDTO {

    // Descripción más detallada del usuario (se inyecta como requerimiento en el prompt).
    private String description;

    // Parámetros estructurados según la categoría del proyecto.
    private ParametersDTO parameters;
}
