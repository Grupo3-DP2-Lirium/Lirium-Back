// InviteCodeValidationResponse.java - Nuevo
package org.example.springboot_backend.dto;

public class InviteCodeValidationResponse {
    private String memorialId;
    private String memorialName;
    private String memorialDescription;
    private String inviterName;
    private Boolean canEdit;
    private Boolean canComment;
    
    // Getters y setters
    public String getMemorialId() { return memorialId; }
    public void setMemorialId(String memorialId) { this.memorialId = memorialId; }
    
    public String getMemorialName() { return memorialName; }
    public void setMemorialName(String memorialName) { this.memorialName = memorialName; }
    
    public String getMemorialDescription() { return memorialDescription; }
    public void setMemorialDescription(String memorialDescription) { this.memorialDescription = memorialDescription; }
    
    public String getInviterName() { return inviterName; }
    public void setInviterName(String inviterName) { this.inviterName = inviterName; }
    
    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }
    
    public Boolean getCanComment() { return canComment; }
    public void setCanComment(Boolean canComment) { this.canComment = canComment; }
}