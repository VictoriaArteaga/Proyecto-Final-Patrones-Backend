package com.proyectofinal.backendapi.dto.project;

import com.proyectofinal.backendapi.model.DesignCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectRequestDTO {

    @NotBlank(message = "El nombre del proyecto es obligatorio.")
    private String name;

    @NotNull(message = "La categoría de diseño es obligatoria.")
    private DesignCategory category;
}
