package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.VideoStatus;
import java.time.LocalDateTime;

@Entity
public class GeneratedVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReel;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String s3key_URL;

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
    public Long getIdReel() { return idReel; }
    public void setIdReel(Long idReel) { this.idReel = idReel; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getS3key_URL() { return s3key_URL; }
    public void setS3key_URL(String s3key_URL) { this.s3key_URL = s3key_URL; }
    public VideoStatus getReelStatus() { return reelStatus; }
    public void setReelStatus(VideoStatus reelStatus) { this.reelStatus = reelStatus; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}