package org.example.springboot_backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class MemoryLiteResponse {
    private UUID idMemory;
    private String title;
    private String description;
    private LocalDate photoDate;
    private LocalDateTime createdDate;

    // opcional: algún thumbnail o primer archivo si lo necesitas
    private String firstFileUrl;
    private String fileType; // raw (image/video/audio) o ya normalizado si prefieres

    public UUID getIdMemory() { return idMemory; }
    public void setIdMemory(UUID idMemory) { this.idMemory = idMemory; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getPhotoDate() { return photoDate; }
    public void setPhotoDate(LocalDate photoDate) { this.photoDate = photoDate; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public String getFirstFileUrl() { return firstFileUrl; }
    public void setFirstFileUrl(String firstFileUrl) { this.firstFileUrl = firstFileUrl; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}
