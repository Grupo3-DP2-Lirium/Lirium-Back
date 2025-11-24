package org.example.springboot_backend.dto;
import java.time.LocalDate;
import java.util.UUID;

public class UserExtraStorageResponse {
    private UUID extraPlanId;
    private String planName;
    private int additionalStorageGb;
    private String status;
    private LocalDate startDate;

    public UserExtraStorageResponse(UUID extraPlanId, String planName, int additionalStorageGb, String status, LocalDate startDate) {
        this.extraPlanId = extraPlanId;
        this.planName = planName;
        this.additionalStorageGb = additionalStorageGb;
        this.status = status;
        this.startDate = startDate;
    }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public int getAdditionalStorageGb() { return additionalStorageGb; }
    public void setAdditionalStorageGb(int additionalStorageGb) { this.additionalStorageGb = additionalStorageGb; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public UUID getExtraPlanId() { return extraPlanId; }
    public void setExtraPlanId(UUID extraPlanId) { this.extraPlanId = extraPlanId; }   
}
