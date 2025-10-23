package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import org.example.springboot_backend.enums.NotificationType;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotification;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    // ID del recurso relacionado (ej: idReminder, idMemorial, etc)
    private Long relatedEntityId;
    
    @Column(nullable = false)
    private boolean isRead = false;
    
    @Column(nullable = false)
    private LocalDateTime createdDate;
    
    private LocalDateTime readDate;

    // Getters and Setters
    public Long getIdNotification() { return idNotification; }
    public void setIdNotification(Long idNotification) { this.idNotification = idNotification; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getReadDate() { return readDate; }
    public void setReadDate(LocalDateTime readDate) { this.readDate = readDate; }
}