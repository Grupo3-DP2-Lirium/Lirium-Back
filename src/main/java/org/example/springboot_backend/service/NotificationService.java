package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.NotificationResponse;
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
    
    /**
     * Crea y envía una notificación de recordatorio
     */
    @Transactional
    public void createReminderNotification(User user, String title, Long reminderId) {
        // Crear notificación en BD
        Notification notification = new Notification();
        notification.setUserId(user.getIdUser());
        notification.setTitle("Recordatorio");
        notification.setMessage(title);
        notification.setType(NotificationType.REMINDER);
        notification.setRelatedEntityId(reminderId);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        // Enviar push notification
        fcmService.sendReminderNotification(user, title, reminderId);
    }
    
    /**
     * Crea y envía una notificación de comentario
     */
    @Transactional
    public void createCommentNotification(User targetUser, String commenterName, 
                                         String comment, Long memorialId) {
        // Crear notificación en BD
        Notification notification = new Notification();
        notification.setUserId(targetUser.getIdUser());
        notification.setTitle("Nuevo comentario");
        notification.setMessage(commenterName + " comentó: " + comment);
        notification.setType(NotificationType.COMMENT);
        notification.setRelatedEntityId(memorialId);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        // Enviar push notification
        fcmService.sendCommentNotification(targetUser, commenterName, comment, memorialId);
    }
    
    /**
     * Crea notificación genérica
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