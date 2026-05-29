package com.proyectofinal.backendapi.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor // Constructor vacío.
@AllArgsConstructor // Constructor con todos los campos.
@Builder
public class ProjectParameters {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

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

    // Elementos opcionales.
    @ElementCollection
    @CollectionTable(name = "project_additional_elements",
            joinColumns = @JoinColumn(name = "parameters_id"))
    @Column(name = "element")
    private List<String> additionalElements;

    // Descripción detallada en texto plan
    @Column
    private String detailDescription;

    // --- Campos para INTERIOR_ROOM y FURNITURE_ITEM (todos opcionales) ---

    // Tipo de habitación: sala, cuarto, cocina, baño, comedor...
    private String roomType;

    // Tipo de mueble u objeto: estantería, sofá, mesa, lámpara...
    private String furnitureType;

    // Dimensiones del mueble en centímetros.
    private Double furnitureWidthCm;
    private Double furnitureHeightCm;
    private Double furnitureDepthCm;

    // Materiales principales: madera, metal, vidrio, tela...
    private String materials;

    // Estilo o tendencia: escandinavo, industrial, minimalista, rústico...
    private String styleTrend;

    // Ubicación dentro de la habitación: contra la pared norte, junto a la ventana...
    private String placement;

    // Establece que estos parámetros pertenecen a un solo proyecto.
    @OneToOne(mappedBy = "parameters") // Relación "dueña" está en la clase.
    private Project project;



}

