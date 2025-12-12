package org.example.springboot_backend.dto;

import org.example.springboot_backend.model.UserReport;
import java.time.LocalDateTime;

public class UserReportResponse {
    private Long id;
    private String reporterUserId;
    private String reporterUserName;
    private String reporterEmail;
    private String reportedUserId;
    private String reportedUserName;
    private String reportedEmail;
    private String reason;
    private String description;
    private String contentType;
    private String contentId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String adminNotes;
    private String resolvedBy;
    private LocalDateTime resolvedAt;

    // Constructor vacío
    public UserReportResponse() {}

    // Constructor desde UserReport
    public UserReportResponse(UserReport report) {
        this.id = report.getId();
        this.reporterUserId = report.getReporterUserId();
        this.reportedUserId = report.getReportedUserId();
        this.reason = report.getReason();
        this.description = report.getDescription();
        this.contentType = report.getContentType();
        this.contentId = report.getContentId();
        this.status = report.getStatus();
        this.createdAt = report.getCreatedAt();
        this.updatedAt = report.getUpdatedAt();
        this.adminNotes = report.getAdminNotes();
        this.resolvedBy = report.getResolvedBy();
        this.resolvedAt = report.getResolvedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReporterUserId() {
        return reporterUserId;
    }

    public void setReporterUserId(String reporterUserId) {
        this.reporterUserId = reporterUserId;
    }

    public String getReporterUserName() {
        return reporterUserName;
    }

    public void setReporterUserName(String reporterUserName) {
        this.reporterUserName = reporterUserName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReportedUserName() {
        return reportedUserName;
    }

    public void setReportedUserName(String reportedUserName) {
        this.reportedUserName = reportedUserName;
    }

    public String getReportedEmail() {
        return reportedEmail;
    }

    public void setReportedEmail(String reportedEmail) {
        this.reportedEmail = reportedEmail;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}