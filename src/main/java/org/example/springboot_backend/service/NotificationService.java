package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.NotificationResponse;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.Notification;
import org.example.springboot_backend.enums.NotificationType;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    /**
     * Obtiene todas las notificaciones de un usuario (no leídas primero)
     */
    public List<NotificationResponse> getUserNotifications(User user) {
        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByUnreadFirstThenDate(user.getIdUser());
        
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene solo notificaciones no leídas
     */
    public List<NotificationResponse> getUnreadNotifications(User user) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedDateDesc(user.getIdUser());
        
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Cuenta las notificaciones no leídas
     */
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndIsReadFalse(user.getIdUser());
    }
    
    /**
     * Marca una notificación como leída
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(user.getIdUser())) {
            throw new RuntimeException("You don't have permission to modify this notification");
        }
        
        notification.setRead(true);
        notification.setReadDate(LocalDateTime.now());
        
        Notification updated = notificationRepository.save(notification);
        return toResponse(updated);
    }
    
    /**
     * Marca todas las notificaciones como leídas
     */
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedDateDesc(user.getIdUser());
        
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadDate(LocalDateTime.now());
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }
    
    /**
     * Elimina una notificación
     */
    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(user.getIdUser())) {
            throw new RuntimeException("You don't have permission to delete this notification");
        }
        
        notificationRepository.delete(notification);
    }
    
    // ========== MÉTODOS PARA AUTO-CREAR NOTIFICACIONES ==========
    
    /**
     * ✅ NUEVO: Notificación al crear un memorial
     */
    @Transactional
    public void notifyMemorialCreated(User creator, Memorial memorial) {
        String title = "Memorial creado";
        String message = String.format("Has creado el memorial '%s'", memorial.getName());
        
        createNotification(
            creator, 
            title, 
            message, 
            NotificationType.SYSTEM, 
            memorial.getIdMemorial().getMostSignificantBits()
        );
    }
    
    /**
     * ✅ NUEVO: Notificación al crear una memoria (simple)
     */
    @Transactional
    public void notifyMemoryCreated(User creator, String memoryTitle, Long memoryId) {
        String title = "Memoria agregada";
        String message = String.format("Has agregado una nueva memoria: '%s'", memoryTitle);
        
        createNotification(
            creator, 
            title, 
            message, 
            NotificationType.SYSTEM, 
            memoryId
        );
    }
    
    /**
     * ✅ NUEVO: Notificación detallada al crear una memoria (con info de archivos)
     */
    @Transactional
    public void notifyMemoryCreatedDetailed(User creator, String detailedMessage, Long memoryId) {
        String title = "Memoria agregada";
        
        createNotification(
            creator, 
            title, 
            detailedMessage, 
            NotificationType.SYSTEM, 
            memoryId
        );
    }
    
    /**
     * ✅ NUEVO: Notificación al actualizar un memorial
     */
    @Transactional
    public void notifyMemorialUpdated(User updater, Memorial memorial) {
        String title = "Memorial actualizado";
        String message = String.format("El memorial '%s' ha sido actualizado", memorial.getName());
        
        createNotification(
            updater, 
            title, 
            message, 
            NotificationType.SYSTEM, 
            memorial.getIdMemorial().getMostSignificantBits()
        );
    }
    
    /**
     * ✅ NUEVO: Notificación a colaboradores cuando se agrega contenido
     */
    @Transactional
    public void notifyCollaborators(Memorial memorial, User excludeUser, String actionMessage) {
        // Aquí deberías obtener la lista de colaboradores del memorial
        // Por ahora, solo notificamos al dueño si no es el que hizo la acción
        if (!memorial.getUser().getIdUser().equals(excludeUser.getIdUser())) {
            String title = "Actividad en memorial";
            String message = String.format("%s en el memorial '%s'", actionMessage, memorial.getName());
            
            createNotification(
                memorial.getUser(), 
                title, 
                message, 
                NotificationType.MEMORIAL_SHARED, 
                memorial.getIdMemorial().getMostSignificantBits()
            );
        }
    }
    
    /**
     * Crea y envía una notificación de recordatorio
     */
    @Transactional
    public void createReminderNotification(User user, String title, Long reminderId) {
        Notification notification = new Notification();
        notification.setUserId(user.getIdUser());
        notification.setTitle("Recordatorio");
        notification.setMessage(title);
        notification.setType(NotificationType.REMINDER);
        notification.setRelatedEntityId(reminderId);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        fcmService.sendReminderNotification(user, title, reminderId);
    }
    
    /**
     * Crea y envía una notificación de comentario
     */
    @Transactional
    public void createCommentNotification(User targetUser, String commenterName, 
                                         String comment, Long memorialId) {
        Notification notification = new Notification();
        notification.setUserId(targetUser.getIdUser());
        notification.setTitle("Nuevo comentario");
        notification.setMessage(commenterName + " comentó: " + comment);
        notification.setType(NotificationType.COMMENT);
        notification.setRelatedEntityId(memorialId);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        fcmService.sendCommentNotification(targetUser, commenterName, comment, memorialId);
    }
    
    /**
     * ✅ MEJORADO: Crea notificación genérica con push notification
     */
    @Transactional
    public void createNotification(User user, String title, String message, 
                                   NotificationType type, Long relatedEntityId) {
        Notification notification = new Notification();
        notification.setUserId(user.getIdUser());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        // Enviar push notification
        Map<String, String> data = new HashMap<>();
        data.put("type", type.toString());
        if (relatedEntityId != null) {
            data.put("relatedEntityId", String.valueOf(relatedEntityId));
        }
        
        fcmService.sendNotificationToUser(user, title, message, data);
    }
    
    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setIdNotification(notification.getIdNotification());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setRelatedEntityId(notification.getRelatedEntityId());
        response.setRead(notification.isRead());
        response.setCreatedDate(notification.getCreatedDate());
        response.setReadDate(notification.getReadDate());
        return response;
    }
}