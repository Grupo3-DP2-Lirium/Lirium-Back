package org.example.springboot_backend.dto;

import java.time.LocalDateTime;

public class ReminderResponse {
    
    private Long idReminder;
    private String title;
    private String description;
    private LocalDateTime notificationDate;
    private boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Constructors
    public ReminderResponse() {
    }

    public ReminderResponse(Long idReminder, String title, String description, 
                           LocalDateTime notificationDate, boolean active, 
                           LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.idReminder = idReminder;
        this.title = title;
        this.description = description;
        this.notificationDate = notificationDate;
        this.active = active;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    // Getters and Setters
    public Long getIdReminder() {
        return idReminder;
    }

    public void setIdReminder(Long idReminder) {
        this.idReminder = idReminder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(LocalDateTime notificationDate) {
        this.notificationDate = notificationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}