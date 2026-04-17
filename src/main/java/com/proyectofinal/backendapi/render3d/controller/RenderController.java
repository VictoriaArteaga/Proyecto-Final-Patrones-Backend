package com.proyectofinal.backendapi.render3d.controller;


import com.proyectofinal.backendapi.render3d.dto.Generate3DRequest;
import com.proyectofinal.backendapi.render3d.dto.TaskStatusResponse;
import com.proyectofinal.backendapi.render3d.service.RenderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


//  Controlador REST para la generación de modelos 3D.
// Endpoints:
// POST /api/render/generate-3d   inicia generación (respuesta inmediata)
// GET  /api/render/status/{id}  consulta estado (para polling)

@Slf4j
@RestController
@RequestMapping("/api/render")
@RequiredArgsConstructor
public class RenderController {

    private final RenderService renderService;

//    Inicia la generación de un modelo 3D a partir de una imagen.
//    El proceso corre en segundo plano. La respuesta devuelve
//    inmediatamente la tarea con status=PENDING y un taskId.
//
//    El cliente debe hacer polling a /api/render/status/{taskId}
//    hasta recibir status=DONE o status=ERROR.

    @PostMapping("/generate-3d")
    public ResponseEntity<TaskStatusResponse> generate3D(
            @Valid @RequestBody Generate3DRequest request
    ) {
        log.info("[RenderController] POST /generate-3d — proyecto={}", request.getProjectId());
        TaskStatusResponse response = renderService.startGeneration(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }


//     Consulta el estado actual de una tarea de generación.
//
//     Posibles estados:
//     - PENDING     en cola, aún no empezó
//     - PROCESSING  enviado a TripoAI, esperando resultado
//     - DONE        modelo listo, resultUrl disponible
//     - ERROR       falló, errorMessage disponible

    @GetMapping("/status/{taskId}")
    public ResponseEntity<TaskStatusResponse> getStatus(
            @PathVariable UUID taskId
    ) {
        log.debug("[RenderController] GET /status/{}", taskId);
        TaskStatusResponse response = renderService.getStatus(taskId);
        return ResponseEntity.ok(response);
    }
}