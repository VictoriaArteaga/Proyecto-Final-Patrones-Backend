package com.proyectofinal.backendapi.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor // Constructor vacío.
@AllArgsConstructor // Constructor con todos los campos.
@Builder
public class ProjectParameters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

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

    // Establece que estos parámetros pertenecen a un solo proyecto.
    @OneToOne(mappedBy = "parameters") // Relación "dueña" está en la clase.
    private Project project;



}

