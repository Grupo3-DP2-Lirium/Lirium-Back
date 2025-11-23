package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.NotificationResponse;
import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.enums.NotificationType;
import org.example.springboot_backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private FCMService fcmService;
    
    // ==================== MÉTODOS DE LECTURA ====================
    
    public List<NotificationResponse> getUserNotifications(User user) {
        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByUnreadFirstThenDate(user.getIdUser());
        
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<NotificationResponse> getUnreadNotifications(User user) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedDateDesc(user.getIdUser());
        
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndIsReadFalse(user.getIdUser());
    }
    
    @Transactional
public NotificationResponse markAsRead(Long notificationId, User user) {
    Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
    
    if (!notification.getUserId().equals(user.getIdUser())) {
        throw new RuntimeException("You don't have permission to modify this notification");
    }
    
    notification.setRead(true);
    
    // ✅ SOLUCIÓN: Usar Instant.now() en lugar de LocalDateTime.now()
    notification.setReadDate(Instant.now());
    
    Notification updated = notificationRepository.save(notification);
    return toResponse(updated);
}
    
    @Transactional
public void markAllAsRead(User user) {
    List<Notification> unreadNotifications = notificationRepository
            .findByUserIdAndIsReadFalseOrderByCreatedDateDesc(user.getIdUser());
    
    // ✅ SOLUCIÓN: Usar Instant.now()
    Instant now = Instant.now();
    
    for (Notification notification : unreadNotifications) {
        notification.setRead(true);
        notification.setReadDate(now);
    }
    
    notificationRepository.saveAll(unreadNotifications);
}
    
    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(user.getIdUser())) {
            throw new RuntimeException("You don't have permission to delete this notification");
        }
        
        notificationRepository.delete(notification);
    }
    
    // ==================== MÉTODO GENÉRICO PRINCIPAL ====================
    
    /**
     * ✅ MÉTODO GENÉRICO: Crea y envía cualquier tipo de notificación
     * 
     * @param user Usuario destinatario
     * @param type Tipo de notificación (SYSTEM, MEMORIAL_SHARED, COMMENT, etc.)
     * @param title Título de la notificación
     * @param message Mensaje descriptivo
     * @param relatedEntityId ID de la entidad relacionada (opcional)
     * @param sendPush Si debe enviar push notification
     */
    @Transactional
    public void createNotification(
            User user, 
            NotificationType type,
            String title, 
            String message, 
            Long relatedEntityId,
            boolean sendPush) {
        
        Notification notification = new Notification();
        notification.setUserId(user.getIdUser());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRead(false);
        
        // ✅ SOLUCIÓN: Usar Instant.now() que siempre es UTC
        notification.setCreatedDate(Instant.now());
        
        notificationRepository.save(notification);
        
        if (sendPush) {
            Map<String, String> data = new HashMap<>();
            data.put("type", type.toString());
            if (relatedEntityId != null) {
                data.put("relatedEntityId", String.valueOf(relatedEntityId));
            }
            
            fcmService.sendNotificationToUser(user, title, message, data);
        }
        
        System.out.println(String.format("✅ Notificación creada: [%s] %s - %s", 
            type, title, message));
    }
    
    /**
     * Sobrecarga simplificada (con push por defecto)
     */
    @Transactional
    public void createNotification(
            User user, 
            NotificationType type,
            String title, 
            String message, 
            Long relatedEntityId) {
        createNotification(user, type, title, message, relatedEntityId, true);
    }
    
    /**
     * Sobrecarga aún más simple (sin relatedEntityId)
     */
    @Transactional
    public void createNotification(
            User user, 
            NotificationType type,
            String title, 
            String message) {
        createNotification(user, type, title, message, null, true);
    }
    
    // ==================== MÉTODOS DE CONVENIENCIA (USAN EL GENÉRICO) ====================
    
    /**
     * ✅ Notificación al crear memorial
     */
    @Transactional
    public void notifyMemorialCreated(User creator, Memorial memorial) {
        createNotification(
            creator,
            NotificationType.SYSTEM,
            "Memorial creado",
            String.format("Has creado el memorial '%s'", memorial.getName()),
            memorial.getIdMemorial().getMostSignificantBits()
        );
    }
    
    /**
     * ✅ Notificación al actualizar memorial
     */
    @Transactional
    public void notifyMemorialUpdated(User updater, Memorial memorial) {
        createNotification(
            updater,
            NotificationType.SYSTEM,
            "Memorial actualizado",
            String.format("El memorial '%s' ha sido actualizado", memorial.getName()),
            memorial.getIdMemorial().getMostSignificantBits()
        );
    }
    
    /**
     * ✅ Notificación al crear memoria (con análisis de archivos)
     * @param creator Usuario que creó la memoria
     * @param memoryTitle Título de la memoria
     * @param memoryId ID de la memoria
     * @param fileDetails Detalles de archivos (ej: "con 2 imágenes, 1 video") - puede ser null
     */
    @Transactional
    public void notifyMemoryCreated(User creator, String memoryTitle, Long memoryId, String fileDetails) {
        String message = fileDetails != null 
            ? String.format("Agregaste '%s' %s", memoryTitle, fileDetails)
            : String.format("Agregaste '%s'", memoryTitle);
        
        createNotification(
            creator,
            NotificationType.SYSTEM,
            "Memoria agregada",
            message,
            memoryId
        );
    }
    
    /**
     * ✅ Notificación a colaboradores
     */
    @Transactional
    public void notifyCollaborators(Memorial memorial, User excludeUser, String actionMessage) {
        if (!memorial.getUser().getIdUser().equals(excludeUser.getIdUser())) {
            createNotification(
                memorial.getUser(),
                NotificationType.MEMORIAL_SHARED,
                "Actividad en memorial",
                String.format("%s en el memorial '%s'", actionMessage, memorial.getName()),
                memorial.getIdMemorial().getMostSignificantBits()
            );
        }
    }
    
    /**
     * ✅ Notificación de recordatorio
     */
    @Transactional
    public void notifyReminder(User user, String reminderTitle, Long reminderId) {
        createNotification(
            user,
            NotificationType.REMINDER,
            "Recordatorio",
            reminderTitle,
            reminderId
        );
    }
    
    /**
     * ✅ Notificación de comentario
     */
    @Transactional
    public void notifyComment(User targetUser, String commenterName, String comment, Long memorialId) {
        createNotification(
            targetUser,
            NotificationType.COMMENT,
            "Nuevo comentario",
            String.format("%s comentó: %s", commenterName, comment),
            memorialId
        );
    }
    
    /**
     * ✅ Notificación de suscripción activada
     */
    @Transactional
    public void notifySubscriptionActivated(User user, String planName, String frequency) {
        createNotification(
            user,
            NotificationType.SUBSCRIPTION,
            "Suscripción activada",
            String.format("Tu suscripción %s (%s) está activa", planName, frequency),
            null
        );
    }
    
    /**
     * ✅ Notificación de suscripción próxima a vencer
     */
    @Transactional
    public void notifySubscriptionExpiring(User user, String planName, int daysRemaining) {
        createNotification(
            user,
            NotificationType.SUBSCRIPTION,
            "Suscripción por vencer",
            String.format("Tu suscripción %s vence en %d días", planName, daysRemaining),
            null
        );
    }

    @Transactional
    public void notifySubscriptionExpired(User user, String planName) {
        createNotification(
            user,
            NotificationType.SUBSCRIPTION,
            "Suscripción expirada",
            String.format("Tu suscripción %s venció ahora", planName),
            null
        );
    }
    
    /**
     * ✅ Notificación de pago exitoso
     */
    @Transactional
    public void notifyPaymentSuccess(User user, double amount, String planName) {
        createNotification(
            user,
            NotificationType.PAYMENT,
            "Pago procesado",
            String.format("Se ha procesado tu pago de $%.2f USD para %s", amount, planName),
            null
        );
    }
    
    /**
     * ✅ Notificación de pago fallido
     */
    @Transactional
    public void notifyPaymentFailed(User user, String reason) {
        createNotification(
            user,
            NotificationType.PAYMENT,
            "Error en pago",
            String.format("No se pudo procesar tu pago: %s", reason),
            null
        );
    }
    
    /**
     * ✅ Notificación de memorial compartido
     */
    @Transactional
    public void notifyMemorialShared(User owner, String shareLink, String memorialName) {
        createNotification(
            owner,
            NotificationType.MEMORIAL_SHARED,
            "Memorial compartido",
            String.format("Has creado un enlace público para '%s'", memorialName),
            null
        );
    }
    
    /**
     * ✅ Notificación de documental generado
     */
    @Transactional
    public void notifyDocumentaryCompleted(User creator, String memorialName, Long documentaryId) {
        createNotification(
            creator,
            NotificationType.SYSTEM,
            "Documental listo",
            String.format("Tu documental de '%s' está disponible", memorialName),
            documentaryId
        );
    }
    
    /**
     * ✅ Notificación de documental fallido
     */
    @Transactional
    public void notifyDocumentaryFailed(User creator, String memorialName, String reason) {
        createNotification(
            creator,
            NotificationType.SYSTEM,
            "Error al generar documental",
            String.format("No se pudo generar el documental de '%s': %s", memorialName, reason),
            null
        );
    }
    
    /**
     * ✅ Notificación de reflexión creada
     */
    @Transactional
    public void notifyReflectionCreated(User creator, String reflectionTitle) {
        createNotification(
            creator,
            NotificationType.SYSTEM,
            "Reflexión guardada",
            String.format("Has guardado la reflexión '%s'", reflectionTitle),
            null
        );
    }
    
    // ==================== UTILIDADES ====================
    
    private NotificationResponse toResponse(Notification notification) {
    NotificationResponse response = new NotificationResponse();
    response.setIdNotification(notification.getIdNotification());
    response.setTitle(notification.getTitle());
    response.setMessage(notification.getMessage());
    response.setType(notification.getType());
    response.setRelatedEntityId(notification.getRelatedEntityId());
    response.setRead(notification.isRead());
    
    // ✅ SOLUCIÓN: Instant ya está en UTC, no necesita conversión
    response.setCreatedDate(notification.getCreatedDate());
    response.setReadDate(notification.getReadDate());
    
    return response;
}

    public void notifyCollaboratorJoined(User memorialOwner, User newCollaborator, Memorial memorial) {
    try {
        String title = "Nuevo colaborador";
        String message = String.format(
            "%s %s se ha unido como colaborador a tu memorial \"%s\"",
            newCollaborator.getFirstName(),
            newCollaborator.getFirstLastName(),
            memorial.getName()
        );
        
        Notification notification = new Notification();
        notification.setUserId(memorialOwner.getIdUser());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(NotificationType.SYSTEM);
        notification.setCreatedDate(Instant.now());
        notification.setRelatedEntityId(memorial.getIdMemorial().getMostSignificantBits());
        
        notificationRepository.save(notification);
        
        System.out.println("✅ Notificación enviada al dueño: " + memorialOwner.getEmail());
    } catch (Exception e) {
        System.err.println("❌ Error creando notificación para dueño: " + e.getMessage());
        // No lanzar excepción para no afectar el flujo principal
    }
}

/**
 * Notifica al usuario que se unió exitosamente como colaborador
 */
public void notifyJoinedAsCollaborator(User collaborator, Memorial memorial) {
    try {
        String title = "Te uniste a un memorial";
        String message = String.format(
            "Ahora eres colaborador del memorial \"%s\". ¡Empieza a compartir recuerdos!",
            memorial.getName()
        );
        
        Notification notification = new Notification();
        notification.setUserId(collaborator.getIdUser());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(NotificationType.SYSTEM);
        notification.setCreatedDate(Instant.now());
        notification.setRelatedEntityId(memorial.getIdMemorial().getMostSignificantBits());
        
        notificationRepository.save(notification);
        
        System.out.println("✅ Notificación enviada al colaborador: " + collaborator.getEmail());
    } catch (Exception e) {
        System.err.println("❌ Error creando notificación para colaborador: " + e.getMessage());
        // No lanzar excepción para no afectar el flujo principal
    }
}

    @Transactional
    public void notifySubscriptionExpired2(User user, String planName) {
        try {
            String title = "Suscripción expirada";
            String message = String.format(
                "Tu suscripción %s ha expirado 😢", 
                planName
            );

            // Crear la notificación en BD
            Notification notification = new Notification();
            notification.setUserId(user.getIdUser());
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(NotificationType.SUBSCRIPTION);
            notification.setCreatedDate(LocalDateTime.now());
            notificationRepository.save(notification);

            createNotification(
                user,
                NotificationType.SYSTEM,
                "Suscripción expirada",
                String.format("Tu suscripción %s ha expirado 😢", planName)
            );
        } catch (Exception e) {
            System.err.println("❌ Error creando notificación de suscripción expirada: " + e.getMessage());
            // No lanzar excepción para no afectar el flujo principal
        }
    }

}