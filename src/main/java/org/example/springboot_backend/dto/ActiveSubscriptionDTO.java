package org.example.springboot_backend.dto;

import java.time.LocalDate;

/**
 * DTO para mostrar información de la suscripción activa del usuario
 */
public class ActiveSubscriptionDTO {
    private String planName;
    private String planType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private Double monthlyPrice;
    private String status;
    private Long daysRemaining;

    // Constructores
    public ActiveSubscriptionDTO() {}

    public ActiveSubscriptionDTO(String planName, String planType, LocalDate startDate,
                                 LocalDate endDate, Boolean isActive, Double monthlyPrice,
                                 String status, Long daysRemaining) {
        this.planName = planName;
        this.planType = planType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.monthlyPrice = monthlyPrice;
        this.status = status;
        this.daysRemaining = daysRemaining;
    }

    // Getters y Setters
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Double getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(Double monthlyPrice) { this.monthlyPrice = monthlyPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(Long daysRemaining) { this.daysRemaining = daysRemaining; }
}