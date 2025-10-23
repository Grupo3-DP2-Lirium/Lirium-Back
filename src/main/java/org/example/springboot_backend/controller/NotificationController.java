package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.springboot_backend.dto.NotificationResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Obtiene todas las notificaciones del usuario
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<NotificationResponse> notifications = notificationService.getUserNotifications(user);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching notifications: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el contador de notificaciones no leídas
     */
    @GetMapping("/unread-count")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            long count = notificationService.getUnreadCount(user);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching unread count: " + e.getMessage());
        }
    }
    
    /**
     * Marca una notificación como leída
     */
    @PatchMapping("/{notificationId}/read")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            NotificationResponse notification = notificationService.markAsRead(notificationId, user);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error marking notification as read: " + e.getMessage());
        }
    }
    
    /**
     * Marca todas las notificaciones como leídas
     */
    @PatchMapping("/mark-all-read")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            notificationService.markAllAsRead(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error marking all as read: " + e.getMessage());
        }
    }
    
    /**
     * Elimina una notificación
     */
    @DeleteMapping("/{notificationId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long notificationId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            notificationService.deleteNotification(notificationId, user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Notification deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error deleting notification: " + e.getMessage());
        }
    }
}