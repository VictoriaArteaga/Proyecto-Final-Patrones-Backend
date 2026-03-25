package com.proyectofinal.backendapi.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // .UUID: Seguridad para web y móvil.
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectState status; // los 9 estados.

    // Usuario relacionado.
    @ManyToOne(fetch = FetchType.LAZY) // Carga los datos del usuario solo cuando se necesitan.
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Versiones relacionadas.
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectVersion> versions;

    // URls
    @Column(length = 1000, nullable = false)
    private String imageOriginalUrl; // Imagen terreno.

    @Column(length = 1000)
    private String image2DUrl; // Imagen 2D AI.

    @Column(length = 1000)
    private String model3DUrl; // Render del modelo 3D.

    // Al borrar el proyecto se Borran los parámetros.
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "parameters_id")
    private ProjectParameters parameters;

    // Fecha automática al crear.
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Fecha automática al editar.
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}