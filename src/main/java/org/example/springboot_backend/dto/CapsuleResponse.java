package org.example.springboot_backend.dto;

import org.example.springboot_backend.enums.CapsuleStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class CapsuleResponse implements Serializable {
    private UUID idCapsule;
    private UUID memorialId;
    private String memorialName;
    private String userPrompt;
    private String title;
    private String description;
    private String musicTrack;
    private String filter;
    private CapsuleStatus status;
    private Integer progress;
    private String videoUrl;
    private String thumbnailUrl;
    private Long videoSize;
    private Integer videoDuration;
    private Integer totalMemories;
    private String errorMessage;
    private LocalDateTime createdDate;
    private LocalDateTime processingCompleted;
    private LocalDateTime publishedDate;
    private LocalDateTime updatedDate;

    // Getters & Setters
    public UUID getIdCapsule() { return idCapsule; }
    public void setIdCapsule(UUID idCapsule) { this.idCapsule = idCapsule; }

    public UUID getMemorialId() { return memorialId; }
    public void setMemorialId(UUID memorialId) { this.memorialId = memorialId; }

    public String getMemorialName() { return memorialName; }
    public void setMemorialName(String memorialName) { this.memorialName = memorialName; }

    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMusicTrack() { return musicTrack; }
    public void setMusicTrack(String musicTrack) { this.musicTrack = musicTrack; }

    public String getFilter() { return filter; }
    public void setFilter(String filter) { this.filter = filter; }

    public CapsuleStatus getStatus() { return status; }
    public void setStatus(CapsuleStatus status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

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

    public LocalDateTime getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDateTime publishedDate) { this.publishedDate = publishedDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}