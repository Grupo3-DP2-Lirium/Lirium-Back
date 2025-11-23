package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
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
    
    private Long relatedEntityId;
    
    @Column(nullable = false)
    private boolean isRead = false;
    
    // ✅ SOLUCIÓN: Usar Instant en lugar de LocalDateTime
    // Instant siempre representa UTC
    @Column(nullable = false)
    private Instant createdDate;
    
    private Instant readDate;
    
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
    
    public Instant getCreatedDate() { return createdDate; }
    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }
    
    public Instant getReadDate() { return readDate; }
    public void setReadDate(Instant readDate) { this.readDate = readDate; }
}