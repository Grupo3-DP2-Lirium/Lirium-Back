package org.example.springboot_backend.dto;

import java.util.UUID;

public class CreateCapsuleRequest {
    private UUID memorialId;
    private String userPrompt; // "Cumpleaños 80 de Lupi", "Navidad 2023"
    private String title;
    private String description;
    private String musicTrack;
    private String filter; // VIVID, DRAMATIC, YELLOW, MONO, SILVERTONE, NATURAL

    // Getters & Setters
    public UUID getMemorialId() { return memorialId; }
    public void setMemorialId(UUID memorialId) { this.memorialId = memorialId; }

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
}