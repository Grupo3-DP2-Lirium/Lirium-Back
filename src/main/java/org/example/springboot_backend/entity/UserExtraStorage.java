package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_extra_storage")
public class UserExtraStorage {

    @Id
    @Column(name = "id_user_extra", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID idUserExtra;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "extra_storage_plan_id")
    private ExtraStoragePlan plan;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public UserExtraStorage() {
        this.idUserExtra = UUID.randomUUID();
    }

    // Getters y setters
    public UUID getIdUserExtra() { return idUserExtra; }
    public void setIdUserExtra(UUID idUserExtra) { this.idUserExtra = idUserExtra; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ExtraStoragePlan getPlan() { return plan; }
    public void setPlan(ExtraStoragePlan plan) { this.plan = plan; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
