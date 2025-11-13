package org.example.springboot_backend.dto;

public class UserExtraStorageResponse {
    private String planName;
    private int additionalStorageGb;
    private String status;

    public UserExtraStorageResponse(String planName, int additionalStorageGb, String status) {
        this.planName = planName;
        this.additionalStorageGb = additionalStorageGb;
        this.status = status;
    }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public int getAdditionalStorageGb() { return additionalStorageGb; }
    public void setAdditionalStorageGb(int additionalStorageGb) { this.additionalStorageGb = additionalStorageGb; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
