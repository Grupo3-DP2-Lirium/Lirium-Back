package org.example.springboot_backend.dto;

import org.example.springboot_backend.enums.MemoryOriginType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MemoryCreateRequest {
    private UUID memorialId;
    private MemoryOriginType type;
    private String title;
    private String description;
    private LocalDate photoDate;
    private LocalDateTime createdDate;
    private String location;
    private boolean visible;
    private List<String> tags;
    private String associatedQuestion;
    private Long questionId;
    private Long answerId;
    private Double latitude;
    private Double longitude;

    public UUID getMemorialId() {
        return memorialId;
    }

    public void setMemorialId(UUID memorialId) {
        this.memorialId = memorialId;
    }

    public MemoryOriginType getType() {
        return type;
    }

    public void setType(MemoryOriginType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getPhotoDate() {
        return photoDate;
    }

    public void setPhotoDate(LocalDate photoDate) {
        this.photoDate = photoDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getAssociatedQuestion() {
        return associatedQuestion;
    }

    public void setAssociatedQuestion(String associatedQuestion) {
        this.associatedQuestion = associatedQuestion;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}