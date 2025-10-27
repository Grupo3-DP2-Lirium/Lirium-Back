package org.example.springboot_backend.dto;

import java.util.UUID;

public class PublicMemoryDto {
    
    private UUID idMemory;
    private String title;
    private String description;
    private String photoDate;
    private String location;
    private String createdDate;
    
    // Constructors
    public PublicMemoryDto() {
    }
    
    // Getters and Setters
    public UUID getIdMemory() {
        return idMemory;
    }
    
    public void setIdMemory(UUID idMemory) {
        this.idMemory = idMemory;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPhotoDate() {
        return photoDate;
    }
    
    public void setPhotoDate(String photoDate) {
        this.photoDate = photoDate;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
