// InviteCodeRequest.java - Reemplaza InviteLinkRequest
package org.example.springboot_backend.dto;

import java.util.UUID;

public class InviteCodeRequest {
    private UUID memorialId;
    private Boolean canEdit = false;
    private Boolean canComment = true;
    private Integer maxUses = 1; // Cuántas veces se puede usar el código
    
    // Getters y setters
    public UUID getMemorialId() { return memorialId; }
    public void setMemorialId(UUID memorialId) { this.memorialId = memorialId; }
    
    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }
    
    public Boolean getCanComment() { return canComment; }
    public void setCanComment(Boolean canComment) { this.canComment = canComment; }
    
    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }
}