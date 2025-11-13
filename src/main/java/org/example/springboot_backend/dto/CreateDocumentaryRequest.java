package org.example.springboot_backend.dto;

import java.util.List;
import java.util.UUID;

public class CreateDocumentaryRequest {
    private UUID memorialId;
    private String title;
    private String description;

    //Enfoque narrativo del usuario
    private String narrativeFocus;

    //Tono emocional
    private String emotionalTone; // nostalgic, joyful, formal, inspiring

    private Integer durationPerMemory = 5;
    private String musicTrack;

    // Valores válidos: warm, classic, modern, natural
    private String styleFilter = "warm";

    private String transitionType = "fade";
    private String resolution = "720p";
    private List<UUID> excludedMemoryIds;

    // Getters y Setters
    public UUID getMemorialId() { return memorialId; }
    public void setMemorialId(UUID memorialId) { this.memorialId = memorialId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNarrativeFocus() { return narrativeFocus; }
    public void setNarrativeFocus(String narrativeFocus) { this.narrativeFocus = narrativeFocus; }

    public String getEmotionalTone() { return emotionalTone; }
    public void setEmotionalTone(String emotionalTone) { this.emotionalTone = emotionalTone; }

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