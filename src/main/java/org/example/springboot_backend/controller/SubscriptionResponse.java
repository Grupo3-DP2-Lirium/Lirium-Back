package org.example.springboot_backend.controller;
import org.example.springboot_backend.dto.UserExtraStorageResponse;
import org.example.springboot_backend.enums.PaymentMethod;
import org.example.springboot_backend.enums.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SubscriptionResponse {
    private UUID subscriptionId;
    private SubscriptionStatus status;
    private String frequency;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private PaymentMethod paymentMethod;
    private Double storageLimitGb;
    private UUID planId;
    private String planName;
    private String planDescription;
    private Double planPrice;
    private String planCurrency;
    private Integer maxFiles; // null = ilimitado
    private Integer maxCollaborations; // null = ilimitado
    private Integer maxDocumentariesPerMonth; // 0 = ninguno
    private Integer documentariesPurchased;
    private Integer documentariesAvailable; 
    private String supportLevel;
    private List<UserExtraStorageResponse> extraStorageSubscriptions;

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
    public Double getStorageLimitGb() {return storageLimitGb;}
    public void setStorageLimitGb(Double storageLimitGb) {this.storageLimitGb = storageLimitGb;}
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
    public Integer getMaxFiles() { return maxFiles; }
    public void setMaxFiles(Integer maxFiles) { this.maxFiles = maxFiles; }
    public Integer getMaxCollaborations() { return maxCollaborations; }
    public void setMaxCollaborations(Integer maxCollaborations) { this.maxCollaborations = maxCollaborations; }
    public Integer getMaxDocumentariesPerMonth() { return maxDocumentariesPerMonth; }
    public void setMaxDocumentariesPerMonth(Integer maxDocumentariesPerMonth) { this.maxDocumentariesPerMonth = maxDocumentariesPerMonth; }
    public String getSupportLevel() { return supportLevel; }
    public void setSupportLevel(String supportLevel) { this.supportLevel = supportLevel; }
    public List<UserExtraStorageResponse> getExtraStorageSubscriptions() { return extraStorageSubscriptions; }
    public void setExtraStorageSubscriptions(List<UserExtraStorageResponse> extraStorageSubscriptions) { this.extraStorageSubscriptions = extraStorageSubscriptions; }
    public Integer getDocumentariesPurchased() { return documentariesPurchased; }
    public void setDocumentariesPurchased(Integer documentariesPurchased) { this.documentariesPurchased = documentariesPurchased; }
    public Integer getDocumentariesAvailable() { return documentariesAvailable; }   
    public void setDocumentariesAvailable(Integer documentariesAvailable) { this.documentariesAvailable = documentariesAvailable; } 
}
