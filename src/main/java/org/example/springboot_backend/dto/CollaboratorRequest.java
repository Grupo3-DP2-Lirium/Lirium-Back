package org.example.springboot_backend.dto;

import java.util.UUID;

public class CollaboratorRequest {
    private UUID memorialId;
    private String email;
    private Boolean canEdit;
    private Boolean canComment;

    // Getters y setters
    public UUID getMemorialId() { return memorialId; }
    public void setMemorialId(UUID memorialId) { this.memorialId = memorialId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }
    
    public Boolean getCanComment() { return canComment; }
    public void setCanComment(Boolean canComment) { this.canComment = canComment; }
}