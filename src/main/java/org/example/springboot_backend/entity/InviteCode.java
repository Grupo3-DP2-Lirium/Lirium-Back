package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.example.springboot_backend.enums.InviteCodeStatusEnum;

@Entity
@Table(name = "invite_codes")
public class InviteCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 8)
    private String code; // Código de 8 caracteres (ej: "A3X9K2M1")
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "memorial_id", nullable = false)
    private Memorial memorial;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(nullable = false)
    private Boolean canEdit = false;
    
    @Column(nullable = false)
    private Boolean canComment = true;
    
    @Column(nullable = false)
    private LocalDateTime createdDate;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt; // Siempre 24 horas
    
    @Column
    private LocalDateTime usedAt;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InviteCodeStatusEnum status = InviteCodeStatusEnum.ACTIVE;
    
    @Column(nullable = false)
    private Integer maxUses = 1;
    
    @Column(nullable = false)
    private Integer usedCount = 0;
    
    // Getters y setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public Memorial getMemorial() { return memorial; }
    public void setMemorial(Memorial memorial) { this.memorial = memorial; }
    
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    
    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }
    
    public Boolean getCanComment() { return canComment; }
    public void setCanComment(Boolean canComment) { this.canComment = canComment; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    
    public InviteCodeStatusEnum getStatus() { return status; }
    public void setStatus(InviteCodeStatusEnum status) { this.status = status; }
    
    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }
    
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
}