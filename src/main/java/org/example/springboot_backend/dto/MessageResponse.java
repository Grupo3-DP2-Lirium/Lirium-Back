package org.example.springboot_backend.dto;

import org.example.springboot_backend.model.Message;
import java.time.LocalDateTime;

public class MessageResponse {
    private Long id;
    private String senderUserId;
    private String senderName;
    private String senderEmail;
    private String subject;
    private String message;
    private String status;
    private String priority;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private LocalDateTime repliedAt;
    private String adminResponse;
    private String adminUserId;
    private String adminName;

    // Constructor vacío
    public MessageResponse() {}

    // Constructor desde Message
    public MessageResponse(Message message) {
        this.id = message.getId();
        this.senderUserId = message.getSenderUserId();
        this.senderName = message.getSenderName();
        this.senderEmail = message.getSenderEmail();
        this.subject = message.getSubject();
        this.message = message.getMessage();
        this.status = message.getStatus();
        this.priority = message.getPriority();
        this.category = message.getCategory();
        this.createdAt = message.getCreatedAt();
        this.readAt = message.getReadAt();
        this.repliedAt = message.getRepliedAt();
        this.adminResponse = message.getAdminResponse();
        this.adminUserId = message.getAdminUserId();
        this.adminName = message.getAdminName();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(LocalDateTime repliedAt) {
        this.repliedAt = repliedAt;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public String getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(String adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
}