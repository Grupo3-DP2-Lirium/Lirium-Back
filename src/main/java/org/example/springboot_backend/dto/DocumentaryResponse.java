package org.example.springboot_backend.dto;

import org.example.springboot_backend.enums.DocumentaryStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class DocumentaryResponse implements Serializable {

    // Identificación y contexto
    private UUID idDocumentary;
    private UUID memorialId;
    private String memorialName;

    // Metadatos visibles
    private String title;
    private String description;

    // Personalización del documental
    private String narrativeFocus;
    private String emotionalTone;     // nostalgic | joyful | formal | inspiring
    private String styleFilter;       // warm | classic | modern | natural
    private String resolution;        // 480p | 720p | 1080p
    private String transitionType;    // fade, etc.
    private Integer durationPerMemory;

    // Proceso y estado
    private DocumentaryStatus status; // DRAFT, PROCESSING, COMPLETED, PUBLISHED, FAILED, CANCELLED
    private Integer progress;         // 0 - 100

    // Resultado de render
    private String videoUrl;
    private String thumbnailUrl;
    private Long videoSize;           // bytes
    private Integer videoDuration;    // segundos
    private Integer totalMemories;

    // Errores y trazabilidad
    private String errorMessage;

    // Timestamps
    private LocalDateTime createdDate;
    private LocalDateTime processingCompleted;
    private LocalDateTime publishedDate;
    private LocalDateTime updatedDate;

    // Getters & Setters
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

    public String getNarrativeFocus() { return narrativeFocus; }
    public void setNarrativeFocus(String narrativeFocus) { this.narrativeFocus = narrativeFocus; }

    public String getEmotionalTone() { return emotionalTone; }
    public void setEmotionalTone(String emotionalTone) { this.emotionalTone = emotionalTone; }

    public String getStyleFilter() { return styleFilter; }
    public void setStyleFilter(String styleFilter) { this.styleFilter = styleFilter; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getTransitionType() { return transitionType; }
    public void setTransitionType(String transitionType) { this.transitionType = transitionType; }

    public Integer getDurationPerMemory() { return durationPerMemory; }
    public void setDurationPerMemory(Integer durationPerMemory) { this.durationPerMemory = durationPerMemory; }

    public DocumentaryStatus getStatus() { return status; }
    public void setStatus(DocumentaryStatus status) { this.status = status; }

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
