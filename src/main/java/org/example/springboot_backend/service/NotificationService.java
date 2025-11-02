package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.NotificationResponse;
import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.enums.NotificationType;
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
        notification.setReadDate(LocalDateTime.now());
        
        Notification updated = notificationRepository.save(notification);
        return toResponse(updated);
    }
    
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
        
        // Crear notificación en base de datos
        Notification notification = new Notification();
        notification.setUserId(user.getIdUser());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        // Enviar push notification si se requiere
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
        response.setCreatedDate(notification.getCreatedDate());
        response.setReadDate(notification.getReadDate());
        return response;
    }
}