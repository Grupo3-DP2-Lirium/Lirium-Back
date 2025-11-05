// ==================== FCM SERVICE ====================
package org.example.springboot_backend.service;

import com.google.firebase.messaging.*;
import org.example.springboot_backend.entity.DeviceToken;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FCMService {
    
    private static final Logger logger = LoggerFactory.getLogger(FCMService.class);
    
    @Autowired
    private DeviceTokenRepository deviceTokenRepository;
    
    /**
     * Registra un nuevo token FCM para un dispositivo
     */
    @Transactional
    public void registerDeviceToken(User user, String fcmToken, String deviceType, String deviceId) {
        // Verificar si el token ya existe
        deviceTokenRepository.findByFcmToken(fcmToken).ifPresentOrElse(
            existingToken -> {
                // Actualizar fecha de último uso
                existingToken.setLastUsedDate(LocalDateTime.now());
                deviceTokenRepository.save(existingToken);
            },
            () -> {
                // Crear nuevo token
                DeviceToken newToken = new DeviceToken();
                newToken.setUserId(user.getIdUser());
                newToken.setFcmToken(fcmToken);
                newToken.setDeviceType(deviceType);
                newToken.setDeviceId(deviceId);
                newToken.setCreatedDate(LocalDateTime.now());
                newToken.setLastUsedDate(LocalDateTime.now());
                deviceTokenRepository.save(newToken);
            }
        );
    }
    
    /**
     * Elimina un token FCM (cuando el usuario cierra sesión)
     */
    @Transactional
    public void unregisterDeviceToken(String fcmToken) {
        deviceTokenRepository.deleteByFcmToken(fcmToken);
    }
    
    /**
     * Envía una notificación push a un usuario específico
     */
    public void sendNotificationToUser(User user, String title, String body, Map<String, String> data) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(user.getIdUser());
        
        if (tokens.isEmpty()) {
            logger.info("No device tokens found for user: {}", user.getEmail());
            return;
        }
        
        for (DeviceToken deviceToken : tokens) {
            try {
                sendNotification(deviceToken.getFcmToken(), title, body, data);
            } catch (Exception e) {
                logger.error("Error sending notification to token: {}", deviceToken.getFcmToken(), e);
                // Si el token es inválido, eliminarlo
                if (e.getMessage() != null && e.getMessage().contains("registration-token-not-registered")) {
                    deviceTokenRepository.delete(deviceToken);
                }
            }
        }
    }
    
    /**
     * Envía una notificación push a un token específico
     */
    private void sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            // Construir el mensaje
            Message.Builder messageBuilder = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                    com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                );
            
            // Agregar datos adicionales si existen
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            // Enviar el mensaje
            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            logger.info("Successfully sent message: {}", response);
            
        } catch (FirebaseMessagingException e) {
            logger.error("Error sending FCM notification", e);
            throw new RuntimeException("Failed to send notification: " + e.getMessage(), e);
        }
    }
    
    /**
     * Envía notificación de recordatorio
     */
    public void sendReminderNotification(User user, String reminderTitle, Long reminderId) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "REMINDER");
        data.put("reminderId", String.valueOf(reminderId));
        
        sendNotificationToUser(
            user,
            "Recordatorio",
            reminderTitle,
            data
        );
    }
    
    /**
     * Envía notificación de comentario
     */
    public void sendCommentNotification(User user, String commenterName, String comment, Long memorialId) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "COMMENT");
        data.put("memorialId", String.valueOf(memorialId));
        
        sendNotificationToUser(
            user,
            "Nuevo comentario",
            commenterName + " comentó: " + comment,
            data
        );
    }
}