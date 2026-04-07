package com.proyectofinal.backendapi.controller;

import com.proyectofinal.backendapi.dto.project.ParametersDTO;
import com.proyectofinal.backendapi.dto.project.ProjectRequestDTO;
import com.proyectofinal.backendapi.dto.project.ProjectResponseDTO;
import com.proyectofinal.backendapi.mapper.ProjectMapper;
import com.proyectofinal.backendapi.model.Project;
import com.proyectofinal.backendapi.model.User;
import com.proyectofinal.backendapi.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;

    // 1. Crear proyecto: subir la imagen inicial.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectResponseDTO> createProject(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute ProjectRequestDTO dto,
            @AuthenticationPrincipal User user
    ) {

        Project project = projectService.createProjectWithImage(file, user, dto.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProjectMapper.toDTO(project));
    }

    // 2. GENERAR RENDER 2D.
    @PostMapping("/{id}/generate-2d")
    public ResponseEntity<ProjectResponseDTO> generate2D(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {

        Project project = projectService.generateInitial2D(id, user);
        return ResponseEntity.ok(ProjectMapper.toDTO(project));
    }

    // 3. APROBAR DISEÑO 2D.
    @PostMapping("/{id}/approve")
    public ResponseEntity<ProjectResponseDTO> approveProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {

        Project project = projectService.approveProject(id, user);
        return ResponseEntity.ok(ProjectMapper.toDTO(project));
    }

    // 4. RECHAZAR DISEÑO 2D.
    @PostMapping("/{id}/reject")
    public ResponseEntity<ProjectResponseDTO> rejectProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {

        Project project = projectService.rejectProject(id, user);
        return ResponseEntity.ok(ProjectMapper.toDTO(project));
    }

    // 5. ACTUALIZAR PARÁMETROS (Después de rechazo)
    @PutMapping("/{id}/parameters")
    public ResponseEntity<ProjectResponseDTO> updateParameters(
            @PathVariable UUID id,
            @Valid @RequestBody ParametersDTO params,
            @AuthenticationPrincipal User user
    ) {

        Project project = projectService.updateParameters(id, user, params);
        return ResponseEntity.ok(ProjectMapper.toDTO(project));
    }

    // 6. GENERAR MODELO 3D.
    @PostMapping("/{id}/generate-3d")
    public ResponseEntity<ProjectResponseDTO> generate3D(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {

        Project project = projectService.generate3D(id, user);
        return ResponseEntity.ok(ProjectMapper.toDTO(project));
    }

    // 7. OBTENER DETALLE DEL PROYECTO POR ID.
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {

        Project project = projectService.getProjectById(id, user);
        return ResponseEntity.ok(ProjectMapper.toDTO(project));
    }

}
