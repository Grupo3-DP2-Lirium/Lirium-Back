package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.PaymentAttemptStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class PaymentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idPaymentAttempt;

    private int userId;
    private int planId;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentAttemptStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column
    private LocalDateTime updatedDate;

    @Column
    private LocalDateTime processedDate;

    @Column
    private String transactionId;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    // getters and setters
    public UUID getIdPaymentAttempt() { return idPaymentAttempt; }
    public void setIdPaymentAttempt(UUID idPaymentAttempt) { this.idPaymentAttempt = idPaymentAttempt; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public PaymentAttemptStatus getStatus() { return status; }
    public void setStatus(PaymentAttemptStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}