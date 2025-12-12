package org.example.springboot_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateUserReportRequest {
    @NotBlank(message = "El ID del usuario reportado es requerido")
    private String reportedUserId;

    @NotBlank(message = "La razón del reporte es requerida")
    private String reason;

    @NotBlank(message = "La descripción es requerida")
    private String description;

    private String contentType;
    private String contentId;

    // Constructors
    public CreateUserReportRequest() {}

    public CreateUserReportRequest(String reportedUserId, String reason, String description, 
                                 String contentType, String contentId) {
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.description = description;
        this.contentType = contentType;
        this.contentId = contentId;
    }

    // Getters and Setters
    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
}