package com.proyectofinal.backendapi.dto.project;

import com.proyectofinal.backendapi.model.DesignCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProjectResponseDTO {

    private UUID id;
    private String name;
    private String status;
    private DesignCategory category;

    private String imageOriginalUrl;
    private String image2DUrl;
    private String model3DUrl;

    private LocalDateTime createdAt;
    private ParametersDTO parameters;

}
