package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Relación N:M entre User y Memorial con permisos
 * Un usuario puede colaborar en múltiples memoriales
 * Un memorial puede tener múltiples colaboradores
 */
@Entity
@Table(
    name = "collaborators",
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_user", "id_memorial"})
)
public class Collaborator {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCollaborator;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_memorial", nullable = false)
    private Memorial memorial;
    
    @Column(nullable = false)
    private Boolean canEdit = false;
    
    @Column(nullable = false)
    private Boolean canComment = true;
    
    @Column(nullable = false)
    private LocalDateTime joinedDate; // Fecha en que aceptó la invitación
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    // Getters y setters
    public Long getIdCollaborator() { return idCollaborator; }
    public void setIdCollaborator(Long idCollaborator) { this.idCollaborator = idCollaborator; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Memorial getMemorial() { return memorial; }
    public void setMemorial(Memorial memorial) { this.memorial = memorial; }
    
    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }
    
    public Boolean getCanComment() { return canComment; }
    public void setCanComment(Boolean canComment) { this.canComment = canComment; }
    
    public LocalDateTime getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDateTime joinedDate) { this.joinedDate = joinedDate; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}