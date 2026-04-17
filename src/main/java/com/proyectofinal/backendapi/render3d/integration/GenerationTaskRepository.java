package com.proyectofinal.backendapi.render3d.integration;


import com.proyectofinal.backendapi.render3d.entity.GenerationTask;
import com.proyectofinal.backendapi.render3d.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GenerationTaskRepository extends JpaRepository<GenerationTask, UUID> {

//    Todas las tareas de un proyecto
    List<GenerationTask> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

//    Tareas pendientes o en proceso (para reintentos al reiniciar)
    List<GenerationTask> findByStatusIn(List<TaskStatus> statuses);
}
