package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.CapsuleFilter;
import org.example.springboot_backend.enums.CapsuleStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "capsules")
public class Capsule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idCapsule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "memorial_id", nullable = false)
    private Memorial memorial;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Prompt del usuario para selección automática de recuerdos
    @Column(nullable = false, columnDefinition = "TEXT")
    private String userPrompt; // Ej: "Cumpleaños 80 de Lupi", "Navidad 2023"

    // Metadatos visibles
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Personalización
    @Column
    private String musicTrack;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CapsuleFilter filter = CapsuleFilter.NATURAL;

    // Estado y proceso
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CapsuleStatus status = CapsuleStatus.DRAFT;

    @Column
    private Integer progress = 0;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // Resultado
    @Column
    private String videoUrl;

    @Column
    private String storagePath;

    @Column
    private String thumbnailUrl;

    @Column
    private Long videoSize; // bytes

    @Column
    private Integer videoDuration; // segundos (max 60)

    @Column
    private Integer totalMemories; // Cantidad de recuerdos usados (8-12)

    // IDs de los recuerdos seleccionados por IA
    @Column(columnDefinition = "TEXT")
    private String memoryIds; // UUIDs separados por coma

    // Timestamps
    @Column
    private LocalDateTime processingStarted;

    @Column
    private LocalDateTime processingCompleted;

    @Column
    private LocalDateTime publishedDate;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Getters & Setters
    public UUID getIdCapsule() { return idCapsule; }
    public void setIdCapsule(UUID idCapsule) { this.idCapsule = idCapsule; }

    public Memorial getMemorial() { return memorial; }
    public void setMemorial(Memorial memorial) { this.memorial = memorial; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMusicTrack() { return musicTrack; }
    public void setMusicTrack(String musicTrack) { this.musicTrack = musicTrack; }

    public CapsuleFilter getFilter() { return filter; }
    public void setFilter(CapsuleFilter filter) { this.filter = filter; }

    public CapsuleStatus getStatus() { return status; }
    public void setStatus(CapsuleStatus status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Long getVideoSize() { return videoSize; }
    public void setVideoSize(Long videoSize) { this.videoSize = videoSize; }

    public Integer getVideoDuration() { return videoDuration; }
    public void setVideoDuration(Integer videoDuration) { this.videoDuration = videoDuration; }

    public Integer getTotalMemories() { return totalMemories; }
    public void setTotalMemories(Integer totalMemories) { this.totalMemories = totalMemories; }

    public String getMemoryIds() { return memoryIds; }
    public void setMemoryIds(String memoryIds) { this.memoryIds = memoryIds; }

    public LocalDateTime getProcessingStarted() { return processingStarted; }
    public void setProcessingStarted(LocalDateTime processingStarted) { this.processingStarted = processingStarted; }

    public LocalDateTime getProcessingCompleted() { return processingCompleted; }
    public void setProcessingCompleted(LocalDateTime processingCompleted) { this.processingCompleted = processingCompleted; }

    public LocalDateTime getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDateTime publishedDate) { this.publishedDate = publishedDate; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}