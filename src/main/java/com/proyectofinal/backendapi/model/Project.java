package com.proyectofinal.backendapi.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "projects")
@Data

public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // .UUID: Seguridad para web y móvil.
    private UUID id;
    private String name;

    @Enumerated(EnumType.STRING)
    private ProjectState status; // los 9 estados.

    // Usuario relacionado.
    @ManyToOne(fetch = FetchType.LAZY) // Carga los datos del usuario solo cuando se necesitan.
    @JoinColumn(name = "User_id", nullable = false)
    private User user;

    // URls
    private String imageOriginalUrl; // Imagen terreno.
    private String image2DUrl; // Imagen 2D AI.
    private String model3DUrl; // Render del modelo 3D.

    // Al borrar el proyecto se Borran los parámetros.
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Parameters_id")
    private ProjectParameters parameters;

    // Fecha automática al crear.
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Fecha automática al editar.
    @UpdateTimestamp
    private LocalDateTime updateAt;
}