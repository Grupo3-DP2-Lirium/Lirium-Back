package org.example.springboot_backend.dto;

import java.time.LocalDateTime;

public class CollaboratorResponse {
    private Long idCollaborator;
    private String email;
    private String userName;
    private Boolean canEdit;
    private Boolean canComment;
    private LocalDateTime invitedDate;
    private LocalDateTime acceptedDate;
    private Boolean isActive;
    private String status; // "active" o "pending"

    // Getters y setters
    public Long getIdCollaborator() { return idCollaborator; }
    public void setIdCollaborator(Long idCollaborator) { this.idCollaborator = idCollaborator; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }
    
    public Boolean getCanComment() { return canComment; }
    public void setCanComment(Boolean canComment) { this.canComment = canComment; }
    
    public LocalDateTime getInvitedDate() { return invitedDate; }
    public void setInvitedDate(LocalDateTime invitedDate) { this.invitedDate = invitedDate; }
    
    public LocalDateTime getAcceptedDate() { return acceptedDate; }
    public void setAcceptedDate(LocalDateTime acceptedDate) { this.acceptedDate = acceptedDate; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}