package org.example.springboot_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.springboot_backend.enums.NotificationType;

import java.time.Instant;

public class NotificationResponse {
    private Long idNotification;
    private String title;
    private String message;
    private NotificationType type;
    private Long relatedEntityId;
    
    @JsonProperty("isRead")
    private boolean read;
    
    // ✅ SOLUCIÓN: Usar Instant en lugar de LocalDateTime
    // Instant siempre representa UTC y se serializa correctamente
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant readDate;

    public NotificationResponse() {}
    
    public NotificationResponse(Long idNotification, String title, String message, 
                              NotificationType type, Long relatedEntityId, 
                              boolean isRead, Instant createdDate, Instant readDate) {
        this.idNotification = idNotification;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedEntityId = relatedEntityId;
        this.read = isRead;
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
    
    @JsonProperty("isRead")
    public boolean isRead() { return read; }
    
    @JsonProperty("isRead")
    public void setRead(boolean read) { this.read = read; }
    
    public Instant getCreatedDate() { return createdDate; }
    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }
    
    public Instant getReadDate() { return readDate; }
    public void setReadDate(Instant readDate) { this.readDate = readDate; }
}