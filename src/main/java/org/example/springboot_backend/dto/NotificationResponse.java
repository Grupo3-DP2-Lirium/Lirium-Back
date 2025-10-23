package org.example.springboot_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.springboot_backend.enums.NotificationType;
import java.time.LocalDateTime;

public class NotificationResponse {
    private Long idNotification;
    private String title;
    private String message;
    private NotificationType type;
    private Long relatedEntityId;
    private boolean isRead;
    
    // ✅ CRÍTICO: Formatear fechas en ISO-8601 para Flutter
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime readDate;

    // Constructors
    public NotificationResponse() {}
    
    public NotificationResponse(Long idNotification, String title, String message, 
                              NotificationType type, Long relatedEntityId, 
                              boolean isRead, LocalDateTime createdDate, LocalDateTime readDate) {
        this.idNotification = idNotification;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedEntityId = relatedEntityId;
        this.isRead = isRead;
        this.createdDate = createdDate;
        this.readDate = readDate;
    }

    // Getters and Setters
    public Long getIdNotification() { return idNotification; }
    public void setIdNotification(Long idNotification) { this.idNotification = idNotification; }
    
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