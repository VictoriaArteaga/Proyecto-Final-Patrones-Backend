package com.proyectofinal.backendapi.service;

import com.proyectofinal.backendapi.model.Notification;
import com.proyectofinal.backendapi.model.User;
import com.proyectofinal.backendapi.repository.NotificationRepository;
import com.proyectofinal.backendapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // Tipos válidos que entiende el frontend.
    private static final Set<String> VALID_TYPES = Set.of("success", "error", "info");

    // Lista del usuario, de la más reciente a la más antigua.
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // Crea una notificación para el usuario (read=false, fecha automática).
    // REQUIRES_NEW: se guarda en su propia transacción, así persiste aunque la
    // transacción que la disparó haga rollback (p. ej. cuando una generación falla).
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification create(User user, String message, String type) {
        String safeType = (type != null && VALID_TYPES.contains(type)) ? type : "info";

        // Referencia ligera al usuario (solo FK) para no depender de la sesión externa.
        User ref = userRepository.getReferenceById(user.getId());

        Notification notification = Notification.builder()
                .user(ref)
                .message(message)
                .type(safeType)
                .read(false)
                .build();

        return notificationRepository.save(notification);
    }

    // Marca todas las del usuario como leídas.
    @Transactional
    public void markAllRead(User user) {
        notificationRepository.markAllReadByUser(user);
    }

    // Borra todas las del usuario.
    @Transactional
    public void clearAll(User user) {
        notificationRepository.deleteByUser(user);
    }
}
