package org.example.springboot_backend.dto;

import org.example.springboot_backend.enums.DocumentaryStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class DocumentaryResponse {
    private UUID idDocumentary;
    private UUID memorialId;
    private String memorialName;
    private String title;
    private String description;
    private DocumentaryStatus status;
    private Integer progress;
    private String videoUrl;
    private Long videoSize;
    private Integer videoDuration;
    private Integer totalMemories;
    private String errorMessage;
    private LocalDateTime createdDate;
    private LocalDateTime processingCompleted;

    // Getters y Setters
    public UUID getIdDocumentary() { return idDocumentary; }
    public void setIdDocumentary(UUID idDocumentary) { this.idDocumentary = idDocumentary; }

    public UUID getMemorialId() { return memorialId; }
    public void setMemorialId(UUID memorialId) { this.memorialId = memorialId; }

    public String getMemorialName() { return memorialName; }
    public void setMemorialName(String memorialName) { this.memorialName = memorialName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DocumentaryStatus getStatus() { return status; }
    public void setStatus(DocumentaryStatus status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Long getVideoSize() { return videoSize; }
    public void setVideoSize(Long videoSize) { this.videoSize = videoSize; }

    public Integer getVideoDuration() { return videoDuration; }
    public void setVideoDuration(Integer videoDuration) { this.videoDuration = videoDuration; }

    public Integer getTotalMemories() { return totalMemories; }
    public void setTotalMemories(Integer totalMemories) { this.totalMemories = totalMemories; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getProcessingCompleted() { return processingCompleted; }
    public void setProcessingCompleted(LocalDateTime processingCompleted) { this.processingCompleted = processingCompleted; }
}