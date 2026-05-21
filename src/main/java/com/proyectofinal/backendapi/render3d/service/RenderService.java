package com.proyectofinal.backendapi.render3d.service;

import com.proyectofinal.backendapi.render3d.dto.Generate3DRequest;
import com.proyectofinal.backendapi.render3d.dto.TaskStatusResponse;
import com.proyectofinal.backendapi.render3d.entity.GenerationTask;
import com.proyectofinal.backendapi.render3d.entity.TaskStatus;
import com.proyectofinal.backendapi.render3d.entity.TaskType;
import com.proyectofinal.backendapi.render3d.integration.GenerationTaskRepository;
import com.proyectofinal.backendapi.service.impl.SupabaseStorageService;
import com.proyectofinal.backendapi.render3d.integration.TripoException;
import com.proyectofinal.backendapi.render3d.integration.TripoClient;
import com.proyectofinal.backendapi.render3d.integration.TripoClient.TripoTaskData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RenderService {

    private static final int POLL_INTERVAL_MS = 5_000;
    private static final int MAX_WAIT_MS      = 480_000;

    private final GenerationTaskRepository taskRepo;
    private final TripoClient              tripoClient;
    private final SupabaseStorageService   supabaseClient;

//  Iniciar la generación.
    @Transactional
    public GenerationTask startGeneration(Generate3DRequest request) {
        log.info("[RenderService] Iniciando generación 3D para proyecto {}", request.getProjectId());

        GenerationTask task = GenerationTask.builder()
                .projectId(request.getProjectId())
                .type(TaskType.MODEL)
                .status(TaskStatus.PENDING)
                .build();

        task = taskRepo.save(task); // Guardamos la tarea

        log.info("[RenderService] Tarea creada: {}", task.getId());

        return task;
    }

    @Transactional(readOnly = true)
    public TaskStatusResponse getStatus(UUID taskId) {
        GenerationTask task = findTaskOrThrow(taskId);
        return toResponse(task);
    }
//    Multitarea
//    Se ejecuta en segundo plano para no bloquear la app.
    @Async("renderExecutor")
    @Transactional
    public void processAsync(UUID taskId, String imageUrl) {

        GenerationTask task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada en Async: " + taskId));

        try {
            log.info("[RenderService] Iniciando llamada asíncrona a Tripo para Task: {}", taskId);
            String tripoTaskId = tripoClient.createImageTo3DTask(imageUrl);

            task.markAsProcessing(tripoTaskId);
            taskRepo.saveAndFlush(task);

            // Espera activa hasta obtener URL válida
            String modelUrl = pollUntilComplete(tripoTaskId, taskId);

            //  Supabase subida
            String supabaseUrl = supabaseClient.downloadAndUpload(modelUrl, task.getProjectId());

            // Guardar éxito
            task.markAsDone(supabaseUrl);
            taskRepo.saveAndFlush(task);
            log.info("[RenderService] Generación completada con éxito. Task={} → URL Supabase", taskId);

        } catch (Exception ex) {
            log.error("[RenderService] Error crítico en segundo plano para tarea {}: {}", taskId, ex.getMessage(), ex);
            task.markAsError(ex.getMessage());
            taskRepo.saveAndFlush(task);
        }
    }


private String pollUntilComplete(String tripoTaskId, UUID localTaskId) {
    long deadline = System.currentTimeMillis() + MAX_WAIT_MS;

    while (System.currentTimeMillis() < deadline) {
        TripoTaskData data = tripoClient.getTaskStatus(tripoTaskId);

        log.info("[Polling] TripoTask={} status={} progress={}%",
                tripoTaskId, data.getStatus(), data.getProgress());

        switch (data.getStatus()) {
            case "success" -> {
                String url = data.getModelUrl();

                if (url != null && !url.isBlank()) {
                    return url;
                }
                log.info("[Polling] Tripo dice 'success' pero la URL no está lista aún. Reintentando...");
                sleep(POLL_INTERVAL_MS);
            }
            case "failed", "cancelled" -> throw new TripoException(
                    "TripoAI: tarea finalizada con error — " + data.getMessage()
            );
            default -> sleep(POLL_INTERVAL_MS);
        }
    }

        throw new TripoException("Timeout: TripoAI tardó más de 5 minutos. Task=" + localTaskId);
    }

    private GenerationTask findTaskOrThrow(UUID taskId) {
        return taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada: " + taskId));
    }

    private TaskStatusResponse toResponse(GenerationTask task) {
        return TaskStatusResponse.builder()
                .taskId(task.getId())
                .projectId(task.getProjectId())
                .type(task.getType())
                .status(task.getStatus())
                .resultUrl(task.getResultUrl())
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}