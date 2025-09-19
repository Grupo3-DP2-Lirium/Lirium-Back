package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.VideoStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class GeneratedVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idReel;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String videoUrl; // URL general donde está almacenado el video

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus reelStatus;

    @Column
    private String type;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column
    private LocalDateTime updatedDate;

    // getters and setters  
    public UUID getIdReel() { return idReel; }
    public void setIdReel(UUID idReel) { this.idReel = idReel; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public VideoStatus getReelStatus() { return reelStatus; }
    public void setReelStatus(VideoStatus reelStatus) { this.reelStatus = reelStatus; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}