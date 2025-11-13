package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.PaymentMethod;
import org.example.springboot_backend.enums.SubscriptionStatus;

import java.time.LocalDateTime;
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

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @Column(name = "frequency", nullable = false)
    private String frequency; // Ej: "MONTHLY" o "YEARLY"

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "current_payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod currentPaymentMethod;

    @Column(name = "paypal_subscription_id", unique = true)
    private String paypalSubscriptionId;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public UserExtraStorage() {
        this.idUserExtra = UUID.randomUUID();
        this.status = SubscriptionStatus.ACTIVE;
    }

    // Getters y Setters
    public UUID getIdUserExtra() { return idUserExtra; }
    public void setIdUserExtra(UUID idUserExtra) { this.idUserExtra = idUserExtra; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ExtraStoragePlan getPlan() { return plan; }
    public void setPlan(ExtraStoragePlan plan) { this.plan = plan; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public PaymentMethod getCurrentPaymentMethod() { return currentPaymentMethod; }
    public void setCurrentPaymentMethod(PaymentMethod currentPaymentMethod) { this.currentPaymentMethod = currentPaymentMethod; }

    public String getPaypalSubscriptionId() { return paypalSubscriptionId; }
    public void setPaypalSubscriptionId(String paypalSubscriptionId) { this.paypalSubscriptionId = paypalSubscriptionId; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
