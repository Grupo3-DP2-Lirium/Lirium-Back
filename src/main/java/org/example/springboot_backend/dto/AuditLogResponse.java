package org.example.springboot_backend.dto;

import org.example.springboot_backend.enums.AuditAction;
import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLogResponse {
    
    private UUID idAuditLog;
    private AuditAction action;
    private String userEmail;
    private UUID userId;
    private String ipAddress;
    private String entityType;
    private String entityId;
    private String details;
    private LocalDateTime createdAt;
    private Boolean success;
    private String errorMessage;

    // Constructors
    public AuditLogResponse() {}

    // Getters and Setters
    public UUID getIdAuditLog() { return idAuditLog; }
    public void setIdAuditLog(UUID idAuditLog) { this.idAuditLog = idAuditLog; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
