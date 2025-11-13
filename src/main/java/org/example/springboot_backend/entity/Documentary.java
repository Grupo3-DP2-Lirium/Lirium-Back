package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.DocumentaryStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documentaries")
public class Documentary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idDocumentary;

    @ManyToOne(optional = false)
    @JoinColumn(name = "memorial_id", nullable = false)
    private Memorial memorial;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Configuración del documental
    @Column(nullable = false)
    private Integer durationPerMemory = 5; // segundos por recuerdo

    @Column
    private String musicTrack; // URL o nombre del track de música

    @Column
    private String styleFilter = "warm"; // warm, sepia, bw, vibrant, etc

    @Column
    private String transitionType = "fade"; // fade, slide, zoom, etc

    // Estado y proceso
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentaryStatus status = DocumentaryStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column
    private Integer progress = 0; // 0-100

    // Resultado
    @Column
    private String videoUrl; // URL del video final en Azure

    @Column
    private String storagePath; // Path en Azure Blob Storage

    @Column
    private Long videoSize; // Tamaño en bytes

    @Column
    private Integer videoDuration; // Duración total en segundos

    @Column
    private String resolution = "720p"; // 480p, 720p, 1080p

    // Metadata del proceso
    @Column
    private Integer totalMemories; // Total de recuerdos incluidos

    @Column(columnDefinition = "TEXT")
    private String memoryIds; // IDs separados por coma de los memories incluidos

    @Column
    private LocalDateTime processingStarted;

    @Column
    private LocalDateTime processingCompleted;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column
    private LocalDateTime updatedDate;

    // Timestamps automáticos
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Getters y Setters
    public UUID getIdDocumentary() { return idDocumentary; }
    public void setIdDocumentary(UUID idDocumentary) { this.idDocumentary = idDocumentary; }

    public Memorial getMemorial() { return memorial; }
    public void setMemorial(Memorial memorial) { this.memorial = memorial; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationPerMemory() { return durationPerMemory; }
    public void setDurationPerMemory(Integer durationPerMemory) { this.durationPerMemory = durationPerMemory; }

    public String getMusicTrack() { return musicTrack; }
    public void setMusicTrack(String musicTrack) { this.musicTrack = musicTrack; }

    public String getStyleFilter() { return styleFilter; }
    public void setStyleFilter(String styleFilter) { this.styleFilter = styleFilter; }

    public String getTransitionType() { return transitionType; }
    public void setTransitionType(String transitionType) { this.transitionType = transitionType; }

    public DocumentaryStatus getStatus() { return status; }
    public void setStatus(DocumentaryStatus status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public Long getVideoSize() { return videoSize; }
    public void setVideoSize(Long videoSize) { this.videoSize = videoSize; }

    public Integer getVideoDuration() { return videoDuration; }
    public void setVideoDuration(Integer videoDuration) { this.videoDuration = videoDuration; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public Integer getTotalMemories() { return totalMemories; }
    public void setTotalMemories(Integer totalMemories) { this.totalMemories = totalMemories; }

    public String getMemoryIds() { return memoryIds; }
    public void setMemoryIds(String memoryIds) { this.memoryIds = memoryIds; }

    public LocalDateTime getProcessingStarted() { return processingStarted; }
    public void setProcessingStarted(LocalDateTime processingStarted) { this.processingStarted = processingStarted; }

    public LocalDateTime getProcessingCompleted() { return processingCompleted; }
    public void setProcessingCompleted(LocalDateTime processingCompleted) { this.processingCompleted = processingCompleted; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}