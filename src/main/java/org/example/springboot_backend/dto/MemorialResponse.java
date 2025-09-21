package org.example.springboot_backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class MemorialResponse {
    private UUID idMemorial;
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String gender;
    private String description;
    private String relationType;
    private String profilePhotoURL;
    private String coverURL;
    private boolean collaborative;
    private boolean journal;
    private Double usedSpace;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Getters y setters
    public UUID getIdMemorial() { return idMemorial; }
    public void setIdMemorial(UUID idMemorial) { this.idMemorial = idMemorial; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public String getProfilePhotoURL() { return profilePhotoURL; }
    public void setProfilePhotoURL(String profilePhotoURL) { this.profilePhotoURL = profilePhotoURL; }
    public String getCoverURL() { return coverURL; }
    public void setCoverURL(String coverURL) { this.coverURL = coverURL; }
    public boolean isCollaborative() { return collaborative; }
    public void setCollaborative(boolean collaborative) { this.collaborative = collaborative; }
    public boolean isJournal() { return journal; }
    public void setJournal(boolean journal) { this.journal = journal; }
    public Double getUsedSpace() { return usedSpace; }
    public void setUsedSpace(Double usedSpace) { this.usedSpace = usedSpace; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
