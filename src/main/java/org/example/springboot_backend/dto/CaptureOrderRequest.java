package org.example.springboot_backend.dto;

import java.util.UUID;

public class CaptureOrderRequest {

    private String orderId;
    private UUID planId;
    private String frequency; // "MONTHLY" o "YEARLY"
    private boolean simulateFail;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public boolean isSimulateFail() {
        return simulateFail;
    }

    public void setSimulateFail(boolean simulateFail) {
        this.simulateFail = simulateFail;
    }
}
