package com.proyectofinal.backendapi.dto.project;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectRequestDTO {
    @NotBlank(message = "El nombre del proyecto es obligatorio.")
    private String name;
}
