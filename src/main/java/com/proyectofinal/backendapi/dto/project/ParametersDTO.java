package com.proyectofinal.backendapi.dto.project;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ParametersDTO {

    // Dimensiones en metros cuadrados.
    private Double lotWidth;
    private Double lotLength;
    private Double totalArea;

    private String constructionType; // Tipo de la construcción.
    private String color;

    // Especificaciones dentro de la construcción.
    private Integer numberOfFloors;
    private Integer numberOfRooms;
    private Integer numberOfBathrooms;

    private List<String> additionalElements;
    private String detailDescription;

    // --- Campos para INTERIOR_ROOM y FURNITURE_ITEM (opcionales) ---
    private String roomType;
    private String furnitureType;
    private Double furnitureWidthCm;
    private Double furnitureHeightCm;
    private Double furnitureDepthCm;
    private String materials;
    private String styleTrend;
    private String placement;
}
