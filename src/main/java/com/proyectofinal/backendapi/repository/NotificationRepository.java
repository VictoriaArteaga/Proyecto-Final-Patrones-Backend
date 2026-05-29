package com.proyectofinal.backendapi.repository;

import com.proyectofinal.backendapi.model.Notification;
import com.proyectofinal.backendapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Notificaciones de un usuario, de la más reciente a la más antigua.
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Marcar todas como leídas en una sola consulta.
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user AND n.read = false")
    void markAllReadByUser(@Param("user") User user);

    // Borrar todas las de un usuario.
    @Modifying
    void deleteByUser(User user);
}
