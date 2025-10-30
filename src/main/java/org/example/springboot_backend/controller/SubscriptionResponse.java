package org.example.springboot_backend.controller;
import org.example.springboot_backend.enums.PaymentMethod;
import org.example.springboot_backend.enums.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class SubscriptionResponse {
    private UUID subscriptionId;
    private SubscriptionStatus status;
    private String frequency;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private PaymentMethod paymentMethod;

    private UUID planId;
    private String planName;
    private String planDescription;
    private Double planPrice;
    private String planCurrency;

    public SubscriptionResponse() {}

    // Getters y setters
    public UUID getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(UUID subscriptionId) { this.subscriptionId = subscriptionId; }
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public UUID getPlanId() { return planId; }
    public void setPlanId(UUID planId) { this.planId = planId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getPlanDescription() { return planDescription; }
    public void setPlanDescription(String planDescription) { this.planDescription = planDescription; }
    public Double getPlanPrice() { return planPrice; }
    public void setPlanPrice(Double planPrice) { this.planPrice = planPrice; }
    public String getPlanCurrency() { return planCurrency; }
    public void setPlanCurrency(String planCurrency) { this.planCurrency = planCurrency; }
}
