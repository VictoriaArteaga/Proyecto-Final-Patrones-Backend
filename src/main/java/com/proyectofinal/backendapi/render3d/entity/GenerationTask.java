package com.proyectofinal.backendapi.render3d.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


// Representa una tarea de generación de modelo 3D.
// Permite rastrear el progreso, manejar errores y procesos largos
// con la IA de TripoAI.

@Entity
@Table(name = "generation_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskType type = TaskType.MODEL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

//    URL del modelo .glb guardado en Supabase Storage.
//    Disponible únicamente cuando status = DONE.

    @Column(name = "result_url", columnDefinition = "TEXT")
    private String resultUrl;

//    ID de la tarea en la API de TripoAI, usado para el polling.

    @Column(name = "tripo_task_id")
    private String tripoTaskId;

//    Mensaje de error si status = ERROR.

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Métodos de negocio ────────────────────────────────────────────────

    public void markAsProcessing(String tripoTaskId) {
        this.tripoTaskId = tripoTaskId;
        this.status = TaskStatus.PROCESSING;
    }

    public void markAsDone(String resultUrl) {
        this.resultUrl = resultUrl;
        this.status = TaskStatus.DONE;
    }

    public void markAsError(String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = TaskStatus.ERROR;
    }
}