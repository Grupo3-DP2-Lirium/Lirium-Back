package org.example.springboot_backend.dto;

import java.util.List;
import java.util.UUID;

public class CreateDocumentaryRequest {
    private UUID memorialId;
    private String title;
    private String description;
    private Integer durationPerMemory = 5; // segundos (3, 5, 8)
    private String musicTrack; // opcional
    private String styleFilter = "warm"; // warm, sepia, bw, vibrant
    private String transitionType = "fade"; // fade, slide, zoom
    private String resolution = "720p"; // 480p, 720p, 1080p
    private List<UUID> excludedMemoryIds; // Memorias a excluir (opcional)

    // Getters y Setters
    public UUID getMemorialId() { return memorialId; }
    public void setMemorialId(UUID memorialId) { this.memorialId = memorialId; }

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

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public List<UUID> getExcludedMemoryIds() { return excludedMemoryIds; }
    public void setExcludedMemoryIds(List<UUID> excludedMemoryIds) { this.excludedMemoryIds = excludedMemoryIds; }
}