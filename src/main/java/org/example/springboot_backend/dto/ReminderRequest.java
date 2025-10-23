package org.example.springboot_backend.dto;

import java.time.LocalDateTime;

public class ReminderRequest {
    
    private String title;
    private String description;
    private LocalDateTime notificationDate;

    // Constructors
    public ReminderRequest() {
    }

    public ReminderRequest(String title, String description, LocalDateTime notificationDate) {
        this.title = title;
        this.description = description;
        this.notificationDate = notificationDate;
    }

    // Getters and Setters
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
}