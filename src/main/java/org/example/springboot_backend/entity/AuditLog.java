package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.AuditAction;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_audit_log")
    private UUID idAuditLog;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;
    
    @Column(name = "user_email")
    private String userEmail;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "entity_type", length = 100)
    private String entityType;
    
    @Column(name = "entity_id")
    private String entityId;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "success")
    private Boolean success;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (success == null) {
            success = true;
        }
    }

    // Constructors
    public AuditLog() {}

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
