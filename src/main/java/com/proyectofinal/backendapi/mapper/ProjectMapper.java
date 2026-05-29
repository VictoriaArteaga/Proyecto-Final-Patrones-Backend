package com.proyectofinal.backendapi.mapper;

import com.proyectofinal.backendapi.dto.project.ParametersDTO;
import com.proyectofinal.backendapi.dto.project.ProjectResponseDTO;
import com.proyectofinal.backendapi.model.Project;

public class ProjectMapper {

    public static ProjectResponseDTO toDTO(Project project) {

        ParametersDTO params = null;

        if (project.getParameters() != null) {
            params = new ParametersDTO();
            params.setLotWidth(project.getParameters().getLotWidth());
            params.setLotLength(project.getParameters().getLotLength());
            params.setTotalArea(project.getParameters().getTotalArea());
            params.setConstructionType(project.getParameters().getConstructionType());
            params.setColor(project.getParameters().getColor());
            params.setNumberOfFloors(project.getParameters().getNumberOfFloors());
            params.setNumberOfRooms(project.getParameters().getNumberOfRooms());
            params.setNumberOfBathrooms(project.getParameters().getNumberOfBathrooms());
            params.setAdditionalElements(project.getParameters().getAdditionalElements());
            params.setDetailDescription(project.getParameters().getDetailDescription());

            // Campos de interior / mueble.
            params.setRoomType(project.getParameters().getRoomType());
            params.setFurnitureType(project.getParameters().getFurnitureType());
            params.setFurnitureWidthCm(project.getParameters().getFurnitureWidthCm());
            params.setFurnitureHeightCm(project.getParameters().getFurnitureHeightCm());
            params.setFurnitureDepthCm(project.getParameters().getFurnitureDepthCm());
            params.setMaterials(project.getParameters().getMaterials());
            params.setStyleTrend(project.getParameters().getStyleTrend());
            params.setPlacement(project.getParameters().getPlacement());
        }

        return ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .status(project.getStatus().name())
                .category(project.getCategory())
                .image2DUrl(project.getImage2DUrl())
                .model3DUrl(project.getModel3DUrl())
                .createdAt(project.getCreatedAt())
                .parameters(params)
                .build();
    }
}
