package com.proyectofinal.backendapi.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Integer versionNumber; // Contador de versiones.

    // URLs de la versión en Supabase
    @Column(length = 1000)
    private String image2DUrl;

    @Column(length = 1000)
    private String model3DUrl;

    // Opcional para ver si la quitamos.
    // Descripción detallada de lo que generó la AI.
    private String aiDescription;

    // Fecha y hora de la versión.
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Relación: Muchas versión pertenecen a un solo proyecto.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

}
