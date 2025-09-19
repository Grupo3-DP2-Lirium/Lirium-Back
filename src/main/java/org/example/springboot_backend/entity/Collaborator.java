package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Collaborator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCollaborator;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Boolean canEdit = false;

    @Column(nullable = false)
    private Boolean canComment = true;

    @Column(nullable = false)
    private LocalDateTime invitedDate;

    @Column
    private LocalDateTime acceptedDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "id_memorial", nullable = false)
    private Memorial memorial;

    @ManyToOne
    @JoinColumn(name = "id_inviter", nullable = false)
    private User inviter;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user; // null if not registered yet

    // getters and setters
    public Long getIdCollaborator() { return idCollaborator; }
    public void setIdCollaborator(Long idCollaborator) { this.idCollaborator = idCollaborator; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
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
    public Memorial getMemorial() { return memorial; }
    public void setMemorial(Memorial memorial) { this.memorial = memorial; }
    public User getInviter() { return inviter; }
    public void setInviter(User inviter) { this.inviter = inviter; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}