package com.proyectofinal.backendapi.repository;

import com.proyectofinal.backendapi.model.Project;
import com.proyectofinal.backendapi.model.ProjectState;
import com.proyectofinal.backendapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // Buscar proyecto por usuario.
    List<Project> findByUser(User user);

    // Buscar proyecto por estado.
    List<Project> findByStatus(ProjectState status);

    // Buscar proyecto específico de un usuario.
    Optional<Project> findByIdAndUser(UUID id, User user);

    // Buscar proyectos por usuario con estado específico.
    List<Project> findByUserAndStatus(User user, ProjectState status);

    // Buscar proyectos de un usuario por fecha de creación.
    List<Project> findByUserOrderByCreatedAtDesc(User user);

    // Buscar proyecto de un usuario por nombre.
    List<Project> findByUserAndNameContainingIgnoreCase(User user, String name);

}