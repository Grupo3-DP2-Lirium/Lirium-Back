package org.example.springboot_backend.dto;

import org.example.springboot_backend.enums.MemoryOriginType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MemoryResponse {
    private UUID idMemory;
    private MemoryOriginType type;
    private String title;
    private String description;
    private LocalDate photoDate;
    private String location;
    private boolean visible;
    private List<String> tags;
    private String associatedQuestion;
    private List<FileResponse> files;
    private Double totalUsedSpace;
    private LocalDateTime createdDate;
    private List<String> categorias;
    private List<String> momentos;
    private boolean esLineaTiempo;
    private Double latitude;
    private Double longitude;

    public UUID getIdMemory() {
        return idMemory;
    }

    public void setIdMemory(UUID idMemory) {
        this.idMemory = idMemory;
    }

    public MemoryOriginType getType() {
        return type;
    }

    public void setType(MemoryOriginType type) {
        this.type = type;
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

    public LocalDate getPhotoDate() {
        return photoDate;
    }

    public void setPhotoDate(LocalDate photoDate) {
        this.photoDate = photoDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getAssociatedQuestion() {
        return associatedQuestion;
    }

    public void setAssociatedQuestion(String associatedQuestion) {
        this.associatedQuestion = associatedQuestion;
    }

    public List<FileResponse> getFiles() {
        return files;
    }

    public void setFiles(List<FileResponse> files) {
        this.files = files;
    }

    public Double getTotalUsedSpace() {
        return totalUsedSpace;
    }

    public void setTotalUsedSpace(Double totalUsedSpace) {
        this.totalUsedSpace = totalUsedSpace;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public List<String> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<String> categorias) {
        this.categorias = categorias;
    }

    public List<String> getMomentos() {
        return momentos;
    }

    public void setMomentos(List<String> momentos) {
        this.momentos = momentos;
    }

    public boolean isEsLineaTiempo() {
        return esLineaTiempo;
    }

    public void setEsLineaTiempo(boolean esLineaTiempo) {
        this.esLineaTiempo = esLineaTiempo;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}