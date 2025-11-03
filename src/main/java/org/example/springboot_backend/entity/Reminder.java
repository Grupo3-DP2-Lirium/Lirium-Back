package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reminders")
public class Reminder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReminder;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // ✅ IMPORTANTE: Este campo se guarda en UTC
    // MySQL lo almacenará como DATETIME sin zona horaria,
    // pero Java lo interpretará como UTC
    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime notificationDate;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdDate;
    
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime updatedDate;
    
    // Getters and Setters
    public Long getIdReminder() { return idReminder; }
    public void setIdReminder(Long idReminder) { this.idReminder = idReminder; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getNotificationDate() { return notificationDate; }
    public void setNotificationDate(LocalDateTime notificationDate) { 
        this.notificationDate = notificationDate; 
    }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}