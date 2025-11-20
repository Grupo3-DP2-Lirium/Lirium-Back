package org.example.springboot_backend.dto;

import java.time.LocalDateTime;

public class SubscriptionResponse {

    private String planName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String frequency;
    private Integer storageLimitGb;

    public SubscriptionResponse(String planName, LocalDateTime startDate,
                                LocalDateTime endDate, String frequency,
                                Integer storageLimitGb) {
        this.planName = planName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.frequency = frequency;
        this.storageLimitGb = storageLimitGb;
    }

    public String getPlanName() { return planName; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public String getFrequency() { return frequency; }
    public Integer getStorageLimitGb() { return storageLimitGb; }
}
