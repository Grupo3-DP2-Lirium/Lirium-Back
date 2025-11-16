// InviteCodeResponse.java - Reemplaza InviteLinkResponse
package org.example.springboot_backend.dto;

import java.time.LocalDateTime;

public class InviteCodeResponse {
    private String code; // Código de 8 dígitos
    private Boolean canEdit;
    private Boolean canComment;
    private LocalDateTime createdDate;
    private LocalDateTime expiresAt;
    private String status;
    private Integer maxUses;
    private Integer usedCount;
    
    // Getters y setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }
    
    public Boolean getCanComment() { return canComment; }
    public void setCanComment(Boolean canComment) { this.canComment = canComment; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }
    
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
}